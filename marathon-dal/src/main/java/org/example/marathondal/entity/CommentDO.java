package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class CommentDO {
    //id
    @TableId
    Long id;

    //用户id
    @TableField("user_id")
    Long userId;

    //赛事id
    @TableField("race_id")
    Long raceId;

    //好评/差评 0:好评 1:差评
    @TableField("type")
    Integer type;

    //评论内容
    @TableField("content")
    String content;

    //评论时间
    @TableField("comment_time")
    LocalDateTime commentTime;

    //评论状态(0:待审核 1:已通过 2:已删除)
    @TableField("status")
    Integer status;

}
