package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.NoticeDO;
import org.example.marathondal.entity.RaceDO;
import org.example.marathondal.entity.RegistrationDO;
import org.example.marathondal.entity.SubscribeDO;
import org.example.marathondal.entity.UserDO;
import org.example.marathondal.mapper.NoticeMapper;
import org.example.marathondal.mapper.RaceMapper;
import org.example.marathondal.mapper.RegistrationMapper;
import org.example.marathondal.mapper.SubscribeMapper;
import org.example.marathondal.mapper.UserMapper;
import org.example.marathonservice.VO.AthleteVO;
import org.example.marathonservice.VO.RegistResultVO;
import org.example.marathonservice.constants.MqConstant;
import org.example.marathonservice.enums.RaceStatus;
import org.example.marathonservice.enums.RegistrationLuaResult;
import org.example.marathonservice.enums.RegistrationMode;
import org.example.marathonservice.enums.RegistrationStatus;
import org.example.marathonservice.message.RegistrationMessage;
import org.example.marathonservice.param.RegistrationParam;
import org.example.marathonservice.utils.RedisIdWorker;
import org.example.marathonservice.utils.WebSocketUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
public class RegistrationService extends ServiceImpl<RegistrationMapper, RegistrationDO> {
    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private RaceMapper raceMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private SubscribeMapper subscribeMapper;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RaceCacheService raceCacheService;

    @Autowired
    private RegistrationLuaExecutor registrationLuaExecutor;

    public RegistrationLuaResult addRegistration(RegistrationParam param) {
        if (param == null || param.getRaceId() == null || param.getUserId() == null) {
            return RegistrationLuaResult.INVALID_PARAM;
        }

        RaceDO race = raceCacheService.getRace(param.getRaceId());
        if (race == null) {
            return RegistrationLuaResult.RACE_NOT_FOUND;
        }
        if (!isRaceEnrolling(race)) {
            return RegistrationLuaResult.RACE_NOT_ENROLLING;
        }

        UserDO user = userMapper.selectById(param.getUserId());
        if (user == null || user.getHealthStatus() == null || user.getHealthStatus() == 0 || user.getHealthStatus() == 5) {
            return RegistrationLuaResult.HEALTH_REQUIRED;
        }
        if (!matchHealthQualification(user.getHealthStatus(), race.getRaceType())) {
            return RegistrationLuaResult.HEALTH_NOT_QUALIFIED;
        }

        raceCacheService.warmRegistrationKeys(race);
        RegistrationLuaResult luaResult = registrationLuaExecutor.tryRegister(param.getRaceId(), param.getUserId());
        if (luaResult != RegistrationLuaResult.SUCCESS) {
            return luaResult;
        }

        RegistrationMessage message = new RegistrationMessage();
        message.setRegistrationId(redisIdWorker.nextId("registration"));
        message.setUserId(param.getUserId());
        message.setRaceId(param.getRaceId());
        message.setRequestTime(LocalDateTime.now());
        message.setIdempotentKey(param.getRaceId() + ":" + param.getUserId());

        try {
            rabbitTemplate.convertAndSend(
                    MqConstant.REGISTRATION_EXCHANGE,
                    MqConstant.REGISTRATION_ROUTING_KEY,
                    message
            );
            return RegistrationLuaResult.SUCCESS;
        } catch (RuntimeException e) {
            registrationLuaExecutor.rollbackUserMarkAndStock(param.getRaceId(), param.getUserId(), race.getRegistrationMode());
            throw e;
        }
    }

    @Transactional
    @RabbitListener(queues = MqConstant.REGISTRATION_QUEUE)
    public void createRegistration(RegistrationMessage message) {
        if (message == null || message.getRaceId() == null || message.getUserId() == null) {
            return;
        }
        RegistrationParam param = new RegistrationParam();
        param.setRaceId(message.getRaceId());
        param.setUserId(message.getUserId());
        if (isRegistered(param)) {
            return;
        }

        RegistrationDO registrationDO = new RegistrationDO();
        registrationDO.setId(message.getRegistrationId() == null ? redisIdWorker.nextId("registration") : message.getRegistrationId());
        registrationDO.setRaceId(message.getRaceId());
        registrationDO.setUserId(message.getUserId());
        registrationDO.setStatus(RegistrationStatus.REGISTERED.getCode());
        registrationDO.setRegisterTime(message.getRequestTime() == null ? LocalDateTime.now() : message.getRequestTime());
        try {
            registrationMapper.insert(registrationDO);
        } catch (DuplicateKeyException ignored) {
            // 数据库唯一索引兜底，重复消息不再创建第二条报名记录。
        }
    }

    public boolean isRegistered(RegistrationParam param) {
        QueryWrapper<RegistrationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", param.getUserId())
                .eq("race_id", param.getRaceId())
                .ne("status", RegistrationStatus.CANCELLED.getCode());
        return registrationMapper.selectCount(queryWrapper) > 0;
    }

    public List<RegistResultVO> list(Long userId) {
        QueryWrapper<RegistrationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<RegistrationDO> registrationDOList = registrationMapper.selectList(queryWrapper);

        List<RegistResultVO> registResultVOList = new ArrayList<>();
        for (RegistrationDO registrationDO : registrationDOList) {
            RaceDO raceDO = raceCacheService.getRace(registrationDO.getRaceId());
            if (raceDO != null) {
                RegistResultVO registResultVO = new RegistResultVO();
                registResultVO.setRaceId(raceDO.getId());
                registResultVO.setRaceName(raceDO.getRaceName());
                registResultVO.setRaceType(raceDO.getRaceType());
                registResultVO.setStatus(registrationDO.getStatus());
                registResultVO.setAthleteNumber(registrationDO.getAthleteNumber());
                registResultVOList.add(registResultVO);
            }
        }
        return registResultVOList;
    }

    public boolean paySuccess(Long userId, Long raceId, Integer raceNumber) {
        UpdateWrapper<RegistrationDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("race_id", raceId)
                .eq("status", RegistrationStatus.LOTTERY_WON_PENDING_PAY.getCode());
        RegistrationDO registrationDO = new RegistrationDO();
        registrationDO.setStatus(RegistrationStatus.PAID.getCode());
        registrationDO.setAthleteNumber(raceNumber);
        return registrationMapper.update(registrationDO, updateWrapper) > 0;
    }

    public int markPayTimeout(Long userId, Long raceId) {
        UpdateWrapper<RegistrationDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("race_id", raceId)
                .eq("status", RegistrationStatus.LOTTERY_WON_PENDING_PAY.getCode());
        RegistrationDO registrationDO = new RegistrationDO();
        registrationDO.setStatus(RegistrationStatus.PAY_TIMEOUT.getCode());
        return registrationMapper.update(registrationDO, updateWrapper);
    }

    public int lottery(Long raceId) {
        RaceDO race = raceMapper.selectById(raceId);
        if (race == null) {
            return 2;
        }
        if (RegistrationMode.fromCode(race.getRegistrationMode()) == RegistrationMode.FIRST_COME) {
            return 3;
        }
        int numOfWinners = race.getParticipantNumber();
        if (LocalDateTime.now().isBefore(race.getEnrollEnd())) {
            return 1;
        }

        Random random = new Random();
        long totalCount = count(new QueryWrapper<RegistrationDO>()
                .eq("race_id", raceId)
                .eq("status", RegistrationStatus.REGISTERED.getCode()));

        List<Long> selectedUserIds = new ArrayList<>();
        if (totalCount <= numOfWinners) {
            UpdateWrapper<RegistrationDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("race_id", raceId).eq("status", RegistrationStatus.REGISTERED.getCode());
            RegistrationDO registrationDO = new RegistrationDO();
            registrationDO.setStatus(RegistrationStatus.LOTTERY_WON_PENDING_PAY.getCode());
            registrationMapper.update(registrationDO, updateWrapper);
            List<RegistrationDO> allRegistrations = registrationMapper.selectList(new QueryWrapper<RegistrationDO>()
                    .eq("race_id", raceId)
                    .eq("status", RegistrationStatus.LOTTERY_WON_PENDING_PAY.getCode()));
            for (RegistrationDO registration : allRegistrations) {
                selectedUserIds.add(registration.getUserId());
            }
        } else {
            List<Long> selectedIds = new ArrayList<>();
            int pageNum = 1;
            int pageSize = 1000;

            while (selectedIds.size() < numOfWinners) {
                Page<RegistrationDO> page = new Page<>(pageNum, pageSize);
                IPage<RegistrationDO> pageResult = page(page, new QueryWrapper<RegistrationDO>()
                        .eq("race_id", raceId)
                        .eq("status", RegistrationStatus.REGISTERED.getCode()));

                List<RegistrationDO> batchRegistrations = pageResult.getRecords();
                if (batchRegistrations.isEmpty()) {
                    break;
                }

                for (int i = 0; i < batchRegistrations.size(); i++) {
                    RegistrationDO registration = batchRegistrations.get(i);
                    if (selectedIds.size() < numOfWinners) {
                        int remaining = numOfWinners - selectedIds.size();
                        int remainingInBatch = batchRegistrations.size() - i;
                        if (random.nextInt(remainingInBatch) < remaining) {
                            selectedIds.add(registration.getId());
                        }
                    }
                }
                pageNum++;
            }

            if (!selectedIds.isEmpty()) {
                UpdateWrapper<RegistrationDO> updateWinners = new UpdateWrapper<>();
                updateWinners.in("id", selectedIds);
                RegistrationDO registrationDO = new RegistrationDO();
                registrationDO.setStatus(RegistrationStatus.LOTTERY_WON_PENDING_PAY.getCode());
                update(registrationDO, updateWinners);
            }

            UpdateWrapper<RegistrationDO> updateLosers = new UpdateWrapper<>();
            updateLosers.eq("race_id", raceId)
                    .eq("status", RegistrationStatus.REGISTERED.getCode())
                    .notIn(!selectedIds.isEmpty(), "id", selectedIds);
            RegistrationDO registrationDO = new RegistrationDO();
            registrationDO.setStatus(RegistrationStatus.LOTTERY_LOST.getCode());
            update(registrationDO, updateLosers);

            List<RegistrationDO> selectedRegistrations = registrationMapper.selectList(new QueryWrapper<RegistrationDO>()
                    .in("id", selectedIds));
            for (RegistrationDO registration : selectedRegistrations) {
                selectedUserIds.add(registration.getUserId());
            }
        }

        sendLotteryResultNotice(selectedUserIds, raceId, true);
        List<RegistrationDO> unselectedRegistrations = registrationMapper.selectList(new QueryWrapper<RegistrationDO>()
                .eq("race_id", raceId)
                .eq("status", RegistrationStatus.LOTTERY_LOST.getCode()));
        List<Long> unselectedUserIds = new ArrayList<>();
        for (RegistrationDO registration : unselectedRegistrations) {
            unselectedUserIds.add(registration.getUserId());
        }
        sendLotteryResultNotice(unselectedUserIds, raceId, false);

        race.setRaceStatus(RaceStatus.LOTTERY_COMPLETED.getCode());
        raceMapper.updateById(race);
        raceCacheService.evictRace(raceId);
        raceCacheService.cacheRace(race);
        return 0;
    }

    public Long count(Long raceId) {
        QueryWrapper<RegistrationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("race_id", raceId);
        return registrationMapper.selectCount(queryWrapper);
    }

    public List<AthleteVO> getAthletesByRaceId(Long raceId) {
        QueryWrapper<RegistrationDO> registrationWrapper = new QueryWrapper<>();
        registrationWrapper.eq("race_id", raceId).eq("status", RegistrationStatus.PAID.getCode());
        List<RegistrationDO> registrationList = registrationMapper.selectList(registrationWrapper);

        List<AthleteVO> athleteVOList = new ArrayList<>();
        for (RegistrationDO registration : registrationList) {
            UserDO user = userMapper.selectById(registration.getUserId());
            if (user != null) {
                AthleteVO athleteVO = new AthleteVO();
                athleteVO.setName(user.getName());
                athleteVO.setAthleteNumber(registration.getAthleteNumber());
                athleteVO.setPhone(user.getPhone());
                athleteVOList.add(athleteVO);
            }
        }
        Collections.sort(athleteVOList, Comparator.comparingInt(AthleteVO::getAthleteNumber));
        return athleteVOList;
    }

    private boolean isRaceEnrolling(RaceDO race) {
        LocalDateTime now = LocalDateTime.now();
        boolean timeWindow = race.getEnrollStart() != null
                && race.getEnrollEnd() != null
                && !now.isBefore(race.getEnrollStart())
                && !now.isAfter(race.getEnrollEnd());
        return timeWindow && (race.getRaceStatus() == null
                || race.getRaceStatus() == RaceStatus.ENROLLING.getCode()
                || race.getRaceStatus() == 2);
    }

    private boolean matchHealthQualification(Integer healthStatus, Integer raceType) {
        if (healthStatus == null || raceType == null || healthStatus == 4) {
            return false;
        }
        if (raceType == 1) {
            return healthStatus <= 1;
        }
        if (raceType == 2) {
            return healthStatus <= 2;
        }
        return healthStatus <= 3;
    }

    private void sendLotteryResultNotice(List<Long> userIds, Long raceId, boolean isWinner) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        NoticeDO notice = new NoticeDO();
        notice.setId(redisIdWorker.nextId("notice"));
        notice.setType(1);
        notice.setCreateTime(LocalDateTime.now());
        String raceName = raceMapper.selectById(raceId).getRaceName();
        notice.setContent(isWinner
                ? "恭喜您，在赛事 " + raceName + " 的抽签中中签！请及时进行缴费。"
                : "很遗憾，您在赛事 " + raceName + " 的抽签中未中签。");
        noticeMapper.insert(notice);

        for (Long userId : userIds) {
            SubscribeDO subscribe = new SubscribeDO();
            subscribe.setId(redisIdWorker.nextId("subscribe"));
            subscribe.setNoticeId(notice.getId());
            subscribe.setUserId(userId);
            subscribe.setIsRead(0);
            subscribeMapper.insert(subscribe);
            WebSocketUtil.sendNotification(userId, raceName + "的抽签结果已出");
        }
    }
}
