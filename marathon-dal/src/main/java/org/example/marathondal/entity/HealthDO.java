package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health")
public class HealthDO {
    //档案id
    @TableId
    Long id;

    //用户id
    @TableField("user_id")
    Long userId;

    /**
     * 0：待审核
     * 1：已审核
     * 2：需复审（材料有误）
     * 3：逻辑删除
     * 4：待定
     */
    //审核状态
    @TableField("audit_status")
    Integer auditStatus;

    //风险等级
    @TableField("risk_level")
    Integer riskLevel;

    //体检报告url
    @TableField("report_url")
    String reportUrl;

    //附件url
    @TableField("appendix_url")
    String appendixUrl;

    //提交时间
    @TableField("submit_time")
    LocalDateTime submitTime;

    //审核有效截止日期
    @TableField("valid_time")
    LocalDateTime validTime;
}
