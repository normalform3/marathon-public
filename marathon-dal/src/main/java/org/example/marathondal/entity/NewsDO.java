package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("news")
public class NewsDO {
    //新闻id
    @TableId
    Long id;

    //新闻标题
    @TableField("title")
    String title;

    //新闻内容
    @TableField("content")
    String content;

    //新闻类型 1:新闻 2:公告
    @TableField("type")
    Integer type;

    //状态 0:隐藏 1:显示
    @TableField("status")
    Integer status;

    //创建时间
    @TableField("create_time")
    LocalDateTime createTime;
}
