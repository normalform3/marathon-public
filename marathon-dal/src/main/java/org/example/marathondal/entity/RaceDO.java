package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("race")
@Data
public class RaceDO {
    @TableId
    Long id;

    //赛事名称
    @TableField("race_name")
    String raceName;

    //赛事信息 简要说明
    @TableField("race_info")
    String raceInfo;

    //赛事类型 1全马 2半马 3:10公里 4:5公里
    @TableField("race_type")
    Integer raceType;

    //赛事编号
    @TableField("race_id")
    String raceId;

    //参赛人数
    @TableField("participant_number")
    Integer participantNumber;

    //报名模式 1:超额报名后抽签 2:限额报名，报完即止
    @TableField("registration_mode")
    Integer registrationMode;

    //报名开始时间
    @TableField("enroll_start")
    LocalDateTime enrollStart;

    //报名结束时间
    @TableField("enroll_end")
    LocalDateTime enrollEnd;

    //费用
    @TableField("fee")
    Double fee;

    //地点
    @TableField("location")
    String location;

    //赛事开始时间
    @TableField("race_date")
    LocalDateTime raceDate;

    //赛事状态 1:未开始 2:报名中 3:报名结束 4:已结束
    @TableField("race_status")
    Integer raceStatus;

    //举办方id
    @TableField("organizer_id")
    Long organizerId;
}
