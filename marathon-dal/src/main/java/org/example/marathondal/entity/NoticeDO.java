package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class NoticeDO {
    //通知id
    @TableId
    private Long id;

    //通知类型 1:抽签结果通知 2:赛事信息变更 3:成绩通知 4:其他
    @TableField("type")
    private Integer type;

    //通知内容
    @TableField("content")
    private String content;

    //创建时间
    @TableField("create_time")
    private LocalDateTime createTime;
}
