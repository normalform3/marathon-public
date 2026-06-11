package org.example.marathonservice.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeVO {
    //订阅id
    private Long subscribeId;

    //通知类型
    private Integer type;

    //通知内容
    private String content;

    //通知时间
    private LocalDateTime createTime;

    //是否已读 0:未读 1:已读
    private Integer isRead;
}
