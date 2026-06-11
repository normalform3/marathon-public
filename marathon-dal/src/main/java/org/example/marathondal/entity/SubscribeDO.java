package org.example.marathondal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("subscribe")
public class SubscribeDO {
    //订阅id
    @TableId
    private Long id;

    //通知id
    @TableField("notice_id")
    private Long noticeId;

    //用户id
    @TableField("user_id")
    private Long userId;

    //是否已读 0:未读 1:已读
    @TableField("is_read")
    private Integer isRead;
}
