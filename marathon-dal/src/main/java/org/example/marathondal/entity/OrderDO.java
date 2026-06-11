package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderDO {
    @TableId
    Long id;

    //用户id
    @TableField("user_id")
    Long userId;

    //赛事id
    @TableField("race_id")
    Long raceId;

    //支付状态 0:待支付 1:已支付 2:已取消
    @TableField("pay_status")
    Integer payStatus;

    //金额
    @TableField("amount")
    Double amount;

    //订单创建时间
    @TableField("create_time")
    LocalDateTime createTime;

    //支付成功时间
    @TableField("pay_time")
    LocalDateTime payTime;
}
