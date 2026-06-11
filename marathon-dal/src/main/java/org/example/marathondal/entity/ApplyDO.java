package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("apply")
public class ApplyDO {
    //申请id
    @TableId
    Long id;

    /**
     * 举办方信息
     */
    //举办方名称
    @TableField("name")
    String name;

    //类型 1政府 2企业 3个人
    @TableField("type")
    Integer type;

    //联系人姓名
    @TableField("contact_name")
    String contactName;

    //联系人电话
    @TableField("contact_phone")
    String contactPhone;

    //联系人邮箱
    @TableField("contact_email")
    String contactEmail;

    //资质材料url
    @TableField("qualification_url")
    String qualificationUrl;

    //授权书url
    @TableField("authorization_url")
    String authorizationUrl;

    /**
     * 赛事信息
     */

    //赛事名称
    @TableField("race_name")
    String raceName;

    //赛事信息 简要说明
    @TableField("race_info")
    String raceInfo;

    //赛事类型 1全马 2半马 3:10公里 4:5公里
    @TableField("race_type")
    Integer raceType;

    //参赛人数
    @TableField("participant_number")
    Integer participantNumber;

    //报名模式 1:超额报名后抽签 2:限额报名，报完即止
    @TableField("registration_mode")
    Integer registrationMode;

    //费用
    @TableField("fee")
    Double fee;

    //地点
    @TableField("location")
    String location;

    //赛事开始时间
    @TableField("race_date")
    LocalDateTime raceDate;

    //举办方id
    @TableField("organizer_id")
    Integer organizerId;

    //申请状态 0:待审核 1:审核通过 2:审核不通过
    @TableField("apply_status")
    Integer applyStatus;

    //申请时间
    @TableField("apply_time")
    LocalDateTime applyTime;
}
