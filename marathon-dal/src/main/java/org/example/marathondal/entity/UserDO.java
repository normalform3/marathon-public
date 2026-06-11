package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("user")
@Data
public class UserDO {
    @TableId
    Long id;

    @TableField("account")
    String account;

    @TableField("password")
    String password;

    @TableField("name")
    String name;

    @TableField("birthday")
    LocalDateTime birthday;

    @TableField("identification_number")
    String identificationNumber;

    @TableField("phone")
    String phone;

    @TableField("emergency_phone")
    String emergencyPhone;

    //身份 1:用户 2:举办方 3:管理员
    @TableField("type")
    Integer type;

    /**
     * 0: 未审核
     * 1: 可参加全马（需有半马完赛记录才能参加
     * 2：可参加半马
     * 3：可参加10公里、5公里赛事
     * 4：身体不适合参加赛事
     * 5：审核中
     */

    // 健康审核状态
    @TableField("health_status")
    Integer healthStatus;
}
