package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.HealthDO;
import org.example.marathondal.entity.UserDO;
import org.example.marathondal.mapper.GradeMapper;
import org.example.marathondal.mapper.HealthMapper;
import org.example.marathondal.mapper.RaceMapper;
import org.example.marathondal.mapper.UserMapper;
import org.example.marathonservice.param.HealthAuditParam;
import org.example.marathonservice.param.ReSubmitParam;
import org.example.marathonservice.utils.RedisIdWorker;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthService extends ServiceImpl<HealthMapper, HealthDO> {
    @Autowired
    private HealthMapper healthMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private RedisIdWorker redisIdWorker;

//    @Autowired
//    private KieContainer kieContainer;

    @Autowired
    private RuleService ruleService;

    //检查是否有已提交的审核，或者已审核且在有效期的记录
    public boolean checkAuditStatus(Long userId) {
        QueryWrapper<HealthDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                    .and(qw -> qw.eq("audit_status", 0)
                                 .or()
                                 .eq("audit_status", 1)
                                 .ge("valid_time", LocalDateTime.now()));
        long count = healthMapper.selectCount(queryWrapper);
        return count > 0;
    }

    //健康评估
    public int audit(HealthAuditParam param) {
        //检查是否有已提交的审核，或者已审核且在有效期的记录
        if (checkAuditStatus(param.getUserId())) {
            return 1; //已有待审核记录 或 已审核且在有效期的记录
        }

        //插入新的审核记录
        HealthDO healthDO = new HealthDO();
        healthDO.setId(redisIdWorker.nextId("health"));
        healthDO.setUserId(param.getUserId());
        healthDO.setAuditStatus(0); //待审核
        healthDO.setReportUrl(param.getReportUrl());
        healthDO.setAppendixUrl(param.getAppendixUrl());
        healthDO.setSubmitTime(LocalDateTime.now());

        /**
         * 进行风险计算
         */
        // 查询用户是否参加过半马、全马
        if(gradeService.checkUserHistoryRace(param.getUserId())){
            param.setPastYearRace(2); // 过去一年参加过半马、全马
        }
        // 进行风险计算
        try {
//            KieSession kieSession = kieContainer.newKieSession();
            //从数据库中动态加载规则
            KieSession kieSession = ruleService.loadEnabledRule();
            System.out.println("规则从DB加载成功");
            kieSession.insert(param);
            kieSession.fireAllRules();
            kieSession.dispose(); // 释放资源,
        } catch (Exception e) {
            e.printStackTrace();
            param.setRiskLevel(0); // 出错时默认未审核
        }
        healthDO.setRiskLevel(param.getRiskLevel());
        healthMapper.insert(healthDO);

        // 更新用户的健康状态为审核中
        UpdateWrapper<UserDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", param.getUserId());
        updateWrapper.set("health_status", 5); // 设置健康状态为审核中
        userMapper.update(null, updateWrapper);

        return 2; //插入成功
    }

    //重新提交材料
    public void reSubmit(ReSubmitParam param) {
        //更新健康档案的审核状态
        UpdateWrapper<HealthDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", param.getDocId());
        updateWrapper.set("audit_status", 0); // 设置审核状态为待审核
        updateWrapper.set("report_url", param.getReportUrl());
        updateWrapper.set("appendix_url", param.getAppendixUrl());
        updateWrapper.set("submit_time", LocalDateTime.now()); // 更新提交时间
        healthMapper.update(null, updateWrapper);
    }


    /**
     * 根据用户id查询全部健康档案
     **/
    public List<HealthDO> getHealthByUserId(Long userId) {
        QueryWrapper<HealthDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return list(queryWrapper);
    }

    /**
     * 查询全部健康档案
     **/
    public List<HealthDO> getAllDocs() {
        QueryWrapper<HealthDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("submit_time");
        return list(queryWrapper);
    }

    /**
     * 通过审核
     **/
    public void pass(Long docId) {
        // 更新健康档案的审核状态 有效期
        UpdateWrapper<HealthDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", docId);
        updateWrapper.set("audit_status", 1); // 设置审核状态为已审核
        updateWrapper.set("valid_time", LocalDateTime.now().plusMonths(2)); // 设置有效期为2个月
        healthMapper.update(null, updateWrapper);

        // 更新用户的健康状态
        HealthDO healthDO = healthMapper.selectById(docId);
        UpdateWrapper<UserDO> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("id", healthDO.getUserId());
        userUpdateWrapper.set("health_status", healthDO.getRiskLevel());
        userMapper.update(null, userUpdateWrapper);
    }

    /**
     * 材料有误 不通过
     **/
    public void notPass(Long docId) {
        // 更新健康档案的审核状态
        UpdateWrapper<HealthDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", docId);
        updateWrapper.set("audit_status", 2); // 设置审核状态为需复审
        healthMapper.update(null, updateWrapper);
    }

    /**
     * 设为待定
     **/
    public void setPending(Long docId) {
        // 更新健康档案的审核状态
        UpdateWrapper<HealthDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", docId);
        updateWrapper.set("audit_status", 4); // 设置审核状态为待定
        healthMapper.update(null, updateWrapper);
    }

    /**
     * 通过但是体检单结果不好
     **/
    public void notQualified(Long docId) {
        // 更新健康档案的审核状态
        UpdateWrapper<HealthDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", docId);
        updateWrapper.set("audit_status", 1); // 设置审核状态为已审核
        updateWrapper.set("valid_time", LocalDateTime.now().plusMonths(2)); // 设置有效期为2个月
        healthMapper.update(null, updateWrapper);
        // 更新用户的健康状态
        HealthDO healthDO = healthMapper.selectById(docId);
        UpdateWrapper<UserDO> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("id", healthDO.getUserId());
        userUpdateWrapper.set("health_status", 4); // 设置健康状态为不合格
        userMapper.update(null, userUpdateWrapper);
    }

    //查询所有超出有效期限的健康评估
    public List<HealthDO> getExpiredHealths() {
        QueryWrapper<HealthDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("valid_time", LocalDateTime.now());
        queryWrapper.eq("audit_status", 1); // 只查询已审核的记录
        return list(queryWrapper);
    }

    //更新用户健康状态
    public void updateHealthStatus(Long userId, Integer status) {
        UpdateWrapper<UserDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId);
        updateWrapper.set("health_status", status);
        userMapper.update(null, updateWrapper);
    }
    //修改档案审核状态
    public void updateDocStatus(Long docId, Integer status) {
        UpdateWrapper<HealthDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", docId);
        updateWrapper.set("audit_status", status);
        healthMapper.update(null, updateWrapper);
    }
}
