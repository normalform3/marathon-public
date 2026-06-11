package org.example.marathonservice.param;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class HealthAuditParam {
    //用户id
    private Long userId;

    /**
     *  个人信息
     */
    //年龄 性别 身高 体重 BMI
    private Integer age;
    private Integer sex; // 1:男 2:女
    private Integer height;
    private Integer weight;
    private Double bmi;

    /**
     *  运动习惯
     */
    //平时是否定期跑步训练 每周跑量 是否有其他运动的习惯 其他运动每周的频率 过去一年内完成过的赛事
    private Integer regularRun; // 1:是 2:否
    private Integer weeklyRunDistance; // 1:5公里以下 2:5-10公里 3:10-20公里 4:20公里以上
    private Integer otherSport; // 1:是 2:否
    private Integer weeklySportTime; // 0:无 1:1-2次 2:3-6次 4:7次以上
    private Integer pastYearRace; // 1:全马 2:半马 3:无

    /**
     *  既往病史
     *  1. 心血管疾病（心脏病、高血压、冠心病等）
     *  呼吸系统疾病（哮喘、慢性阻塞性肺病等）
     *  代谢性疾病（糖尿病、高血脂等）
     *  神经系统疾病（癫痫、中风病史等）
     *
     *  2. 近期是否有严重外伤、骨折、关节问题
     *  3. 过去一年内是否因健康问题接受住院治疗
     *  4. 是否有医生建议你不要剧烈运动
     *  5. 直系亲属（父母、兄弟姐妹）是否有心血管疾病史
     *  6. 有无家族猝死史
     */
    private Integer haveChronicDisease; // 1:是 2:否
    private Integer haveBoneInjury; // 1:是 2:否
    private Integer haveHospitalized; // 1:是 2:否
    private Integer haveDoctorAdvice; // 1:是 2:否
    private Integer haveFamilyChronicDisease; // 1:是 2:否
    private Integer haveFamilySuddenDeath; // 1:是 2:否

    /**
     *  体检报告数据
     *  血压：收缩压 舒张压
     *  心电图诊断是否异常
     *  心率
     */
    private Integer lowPressure;
    private Integer highPressure;
    private Integer ecg; // 1:正常 2:异常
    private Integer heartRate;

    /**
     * 是否请求人工复审
     */
    private Integer needReview; // 1:是 2:否

    //体检报告url
    private String reportUrl;

    //附件url
    private String appendixUrl;

    /**
     * 0: 未审核
     * 1: 可参加全马（需有半马完赛记录才能参加
     * 2：可参加半马
     * 3：可参加10公里、5公里赛事
     * 4：身体不适合参加赛事
     * 5：审核中
     */

    // 健康审核状态
    private Integer riskLevel;

}
