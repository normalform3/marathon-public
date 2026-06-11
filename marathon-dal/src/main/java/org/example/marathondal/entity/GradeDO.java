package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("grade")
public class GradeDO {
    // id
    @TableId
    Long id;

    // 用户id
    @TableField("user_id")
    Long userId;

    // 赛事id
    @TableField("race_id")
    Long raceId;

    // 参赛号
    @TableField("athlete_number")
    Integer athleteNumber;

    // 成绩 (格式 hh:mm:ss
    @TableField("grade")
    String grade;

    // 排名
    @TableField("`rank`")
    private Integer rank;

    // 是否评论过 0:未评论 已评论则存评论id
    @TableField("is_comment")
    Long isComment;
}
