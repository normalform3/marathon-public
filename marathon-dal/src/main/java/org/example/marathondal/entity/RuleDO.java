package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rule")
public class RuleDO {
    //规则id
    @TableId
    Long id;

    //规则名称
    @TableField("name")
    String name;

    //规则内容
    @TableField("content")
    String content;

    //状态 0:未启用 1:启用 （同时只能有一个启用）
    @TableField("status")
    Integer status;

    //创建时间
    @TableField("create_time")
    LocalDateTime createTime;
}
