package org.example.marathonservice.param;

import lombok.Data;
import org.example.marathondal.entity.CommentDO;

@Data
public class CommentParam {
    //成绩id
    Long gradeId;

    //评论体
    CommentDO comment;
}
