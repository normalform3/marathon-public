package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.*;
import org.example.marathondal.mapper.*;
import org.example.marathonservice.VO.AthleteExcel;
import org.example.marathonservice.VO.AthleteVO;
import org.example.marathonservice.VO.GradeVO;
import org.example.marathonservice.utils.RedisIdWorker;
import org.example.marathonservice.utils.WebSocketUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GradeService extends ServiceImpl<GradeMapper, GradeDO> {
    @Autowired
    private RaceMapper raceMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private SubscribeMapper subscribeMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private RedisIdWorker redisIdWorker;

    // 查询用户成绩 通过用户id
    public List<GradeVO> list(Long id) {
        List<GradeDO> gradeDOList = this.lambdaQuery().eq(GradeDO::getUserId, id).list();
        List<GradeVO> gradeVOList = new ArrayList<>();

        // 提取所有 raceId，进行显式类型转换
        List<Long> raceIds = gradeDOList.stream()
                .map(GradeDO::getRaceId)
                .collect(Collectors.toList());

        // 检查 raceIds 是否为空
        if (raceIds.isEmpty()) {
            return gradeVOList;
        }

        // 批量查询 RaceDO
        List<RaceDO> raceDOList = raceMapper.selectBatchIds(raceIds);

        // 将 RaceDO 转换为 Map 以便快速查找
        Map<Long, RaceDO> raceMap = raceDOList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(RaceDO::getId, r -> r, (first, second) -> first));

        // 查询用户信息
        UserDO userDO = userMapper.selectById(id);
        String userName = userDO != null ? userDO.getName() : null;

        for (GradeDO gradeDO : gradeDOList) {
            GradeVO gradeVO = new GradeVO();
            BeanUtils.copyProperties(gradeDO, gradeVO);

            // 根据 raceId 获取 RaceDO 对象
            RaceDO raceDO = raceMap.get(gradeDO.getRaceId().longValue()); // 确保使用 Long 类型进行查找
            if (raceDO != null) {
                gradeVO.setRaceName(raceDO.getRaceName());
                gradeVO.setRaceType(raceDO.getRaceType());
                gradeVO.setRaceDate(raceDO.getRaceDate());
            }

            // 设置用户姓名
            gradeVO.setName(userName);

            gradeVOList.add(gradeVO);
        }
        //根据赛事日期降序排列
        gradeVOList.sort(Comparator.comparing(
                GradeVO::getRaceDate,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return gradeVOList;
    }

    // 检查用户是否参加过全马或半马
    public boolean checkUserHistoryRace(Long userId) {
        // 先查询用户参加的所有赛事 ID
        QueryWrapper<GradeDO> gradeWrapper = new QueryWrapper<>();
        gradeWrapper.eq("user_id", userId);
        List<GradeDO> gradeList = gradeMapper.selectList(gradeWrapper);

        // 提取赛事 ID
        List<Long> raceIds = gradeList.stream().map(GradeDO::getRaceId).toList();

        // 如果没有查询到赛事 ID，直接返回 false
        if (raceIds.isEmpty()) {
            return false;
        }

        // 根据赛事 ID 查询赛事类型
        QueryWrapper<RaceDO> raceWrapper = new QueryWrapper<>();
        raceWrapper.in("id", raceIds).in("race_type", 1, 2);
        Long count = raceMapper.selectCount(raceWrapper);

        return count > 0;
    }

    //  生成成绩数据模板
    public List<AthleteExcel> getAthleteList(Long raceId) {
        List<AthleteExcel> athletes = new ArrayList<>();

        // 查询报名表中当前 raceid 且 status 为 5 的报名信息
        QueryWrapper<RegistrationDO> registrationWrapper = new QueryWrapper<>();
        registrationWrapper.eq("race_id", raceId).eq("status", 5);
        List<RegistrationDO> registrationList = registrationMapper.selectList(registrationWrapper);

        //构建成绩数据
        for (RegistrationDO registration : registrationList) {
            // 根据 userId 查询用户信息
            Long userId = registration.getUserId();
            UserDO user = userMapper.selectById(userId);

            if (user != null) {
                AthleteExcel athlete = new AthleteExcel();
                athlete.setRaceNumber(registration.getAthleteNumber());
                athlete.setName(user.getName());
                athletes.add(athlete);
            }
        }
        // 按照参赛号升序排列
        Collections.sort(athletes, Comparator.comparingInt(AthleteExcel::getRaceNumber));
        return athletes;
    }

    //读入成绩，存入数据库
    public void saveGrades(List<AthleteExcel> list, Long raceId) {
        List<GradeDO> grades = new ArrayList<>();
        List<Long> userIds = new ArrayList<>(); // 用于存储被录入成绩的用户 ID

        for (AthleteExcel athlete : list) {
            QueryWrapper<RegistrationDO> registrationWrapper = new QueryWrapper<>();
            //根据报名表中的参赛号和赛事id查询用户id
            registrationWrapper.eq("athlete_number", athlete.getRaceNumber())
                    .eq("race_id", raceId);
            RegistrationDO registration = registrationMapper.selectOne(registrationWrapper);

            if (registration == null) {
                continue;
            }

            GradeDO gradeDO = new GradeDO();
            gradeDO.setId(redisIdWorker.nextId("grade"));
            gradeDO.setUserId(registration.getUserId());
            gradeDO.setRaceId(raceId);
            gradeDO.setAthleteNumber(athlete.getRaceNumber());
            gradeDO.setGrade(athlete.getScore());
            gradeDO.setIsComment(0L); // 默认未评论
            grades.add(gradeDO);
            userIds.add(registration.getUserId()); // 记录用户 ID
        }

        // 排序并设置排名
        grades.sort(Comparator.comparing(g -> convertToSeconds(g.getGrade())));

        int rank = 1;
        for (GradeDO g : grades) {
            g.setRank(rank++);
        }

        // 批量插入
        this.saveBatch(grades);

        // 发送成绩录入通知
        sendGradeEntryNotice(userIds, raceId);
    }

    // 发送成绩录入通知
    private void sendGradeEntryNotice(List<Long> userIds, Long raceId) {
        NoticeDO notice = new NoticeDO();
        notice.setId(redisIdWorker.nextId("notice"));
        notice.setType(3); // 成绩录入通知类型为 3
        notice.setCreateTime(LocalDateTime.now());
        // 查询赛事名称
        String raceName = raceMapper.selectById(raceId).getRaceName();
        notice.setContent("您参加的赛事 " + raceName + " 的成绩已录入，请查看。");
        noticeMapper.insert(notice);

        for (Long userId : userIds) {
            SubscribeDO subscribe = new SubscribeDO();
            subscribe.setId(redisIdWorker.nextId("subscribe"));
            subscribe.setNoticeId(notice.getId());
            subscribe.setUserId(userId);
            subscribe.setIsRead(0);
            subscribeMapper.insert(subscribe);

            // 使用工具类发送通知
            try {
                WebSocketUtil.sendNotification(userId, notice.getContent());
            } catch (Exception e) {
                System.out.println("WebSocket 发送通知失败：" + e.getMessage());
            }
        }
    }


    //将 hh:mm:ss 转换为秒
    public static int convertToSeconds(String timeStr) {
        try {
            // 补齐时间格式为 HH:mm:ss
            String[] parts = timeStr.split(":");
            if (parts.length == 2) {
                timeStr = "00:" + timeStr; // mm:ss -> 00:mm:ss
            } else if (parts.length == 1) {
                timeStr = "00:00:" + timeStr; // ss -> 00:00:ss
            } else if (parts.length == 3 && parts[0].length() == 1) {
                timeStr = "0" + timeStr; // 1:02:03 -> 01:02:03
            }

            LocalTime time = LocalTime.parse(timeStr);
            return time.toSecondOfDay();
        } catch (Exception e) {
            System.out.println("时间解析失败：" + timeStr);
            return Integer.MAX_VALUE;
        }
    }


}
