package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route")
public class RouteDO {
    //路线id
    @TableId
    Long id;

    //赛事id
    @TableField("race_id")
    Long raceId;

    //路线数组
    @TableField("route")
    String route;

    //路线状态(待定
    @TableField("status")
    Integer status;

    //创建时间
    @TableField("create_time")
    LocalDateTime createTime;
}
