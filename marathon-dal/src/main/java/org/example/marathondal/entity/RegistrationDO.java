package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("registration")
public class RegistrationDO {
    //没有自增长 需要手动生成
    @TableId
    Long id;

    //用户id
    @TableField("user_id")
    Long userId;

    //赛事id
    @TableField("race_id")
    Long raceId;

    //报名状态 1:已报名 2:已取消 3:未中签 4:已中签（未支付） 5:已支付
    @TableField("status")
    Integer status;

    //参赛号
    @TableField("athlete_number")
    Integer athleteNumber;

    //报名时间
    @TableField("register_time")
    LocalDateTime registerTime;
}
