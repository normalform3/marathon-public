package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.ApplyDO;
import org.example.marathondal.entity.RaceDO;
import org.example.marathondal.entity.UserDO;
import org.example.marathondal.mapper.ApplyMapper;
import org.example.marathondal.mapper.RaceMapper;
import org.example.marathondal.mapper.UserMapper;
import org.example.marathonservice.enums.RegistrationMode;
import org.example.marathonservice.utils.EmailUtil;
import org.example.marathonservice.utils.RaceIdUtil;
import org.example.marathonservice.utils.RedisIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ApplyService extends ServiceImpl<ApplyMapper, ApplyDO> {
    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private RaceMapper raceMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailUtil emailUtil;

    //提交申请
    public boolean submit(ApplyDO applyDO) {
        applyDO.setId(redisIdWorker.nextId("apply"));
        applyDO.setApplyStatus(0); //待审核
        applyDO.setApplyTime(LocalDateTime.now());
        return this.save(applyDO);
    }

    //查看全部申请
    public List<ApplyDO> getAllApply() {
        QueryWrapper<ApplyDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("apply_time");
        return list(queryWrapper);
    }

    //通过申请
    public boolean approve(Long applyId) {
        ApplyDO applyDO = this.getById(applyId);
        if (applyDO == null) {
            return false;
        }

        // 创建赛事
        RaceDO raceDO = new RaceDO();
        raceDO.setId(redisIdWorker.nextId("race"));
        raceDO.setRaceName(applyDO.getRaceName());
        raceDO.setRaceInfo(applyDO.getRaceInfo());
        raceDO.setRaceType(applyDO.getRaceType());
        raceDO.setParticipantNumber(applyDO.getParticipantNumber());
        raceDO.setRegistrationMode(RegistrationMode.fromCode(applyDO.getRegistrationMode()).getCode());
        raceDO.setFee(applyDO.getFee());
        raceDO.setLocation(applyDO.getLocation());
        raceDO.setRaceDate(applyDO.getRaceDate());
        // 报名开始时间为赛事时间的前2个月
        raceDO.setEnrollStart(applyDO.getRaceDate().minusMonths(2));
        // 报名持续2周
        raceDO.setEnrollEnd(raceDO.getEnrollStart().plusWeeks(2));
        raceDO.setRaceStatus(1);

        //生成赛事编号
        raceDO.setRaceId(RaceIdUtil.generateRaceId(applyDO.getLocation(), applyDO.getRaceDate()));

        // 创建举办方账号
        // 生成随机6位账号密码
        UserDO userDO = new UserDO();
        userDO.setId(redisIdWorker.nextId("user"));
        String account = String.valueOf((int) (Math.random() * 1000000));
        String password = String.valueOf((int) (Math.random() * 1000000));
        userDO.setAccount(account);
        userDO.setPassword(password);
        userDO.setName(applyDO.getName());
        userDO.setPhone(applyDO.getContactPhone());
        userDO.setType(2); // 2表示举办方

        raceDO.setOrganizerId(userDO.getId());

        // 保存赛事和举办方账号
        raceMapper.insert(raceDO);
        userMapper.insert(userDO);

        //发送账号密码到邮箱 通知审核通过
        try {
            emailUtil.sendSimpleEmail(applyDO.getContactEmail(), "您的马拉松赛事举办申请审核通过", "您的账号为：" + account + "\n密码为：" + password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 更新申请状态为已通过
        applyDO.setApplyStatus(1);
        return this.updateById(applyDO);
    }

    //拒绝申请
    public boolean reject(Long applyId) {
        ApplyDO applyDO = this.getById(applyId);
        if (applyDO == null) {
            return false;
        }
        applyDO.setApplyStatus(2); // 2表示拒绝
        // 发送拒绝通知到邮箱
        emailUtil.sendSimpleEmail(applyDO.getContactEmail(), "您的马拉松赛事举办申请审核未通过", "很抱歉，您的申请未通过审核，请重新申请");
        return this.updateById(applyDO);
    }

}
