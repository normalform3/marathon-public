package org.example.marathonservice.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.CommentDO;
import org.example.marathondal.mapper.CommentMapper;
import org.example.marathondal.mapper.GradeMapper;
import org.example.marathonservice.param.CommentParam;
import org.example.marathonservice.utils.RedisIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService extends ServiceImpl<CommentMapper, CommentDO> {
    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private GradeMapper gradeMapper;

    // 用户提交评论
    public boolean submit(CommentParam param) {
        CommentDO comment = param.getComment();
        comment.setId(redisIdWorker.nextId("comment"));
        //更新成绩的评论情况
        gradeMapper.updateIsComment(param.getGradeId(), comment.getId());

        comment.setCommentTime(LocalDateTime.now());
        comment.setStatus(1); // 默认状态为通过审核
        return this.save(comment);
    }

    // 根据赛事id查看评论
    public List<CommentDO> getByRaceId(Long raceId) {
        return this.lambdaQuery().eq(CommentDO::getRaceId, raceId).list();
    }

    // 逻辑删除评论
    public boolean logicDelete(Long id) {
        CommentDO comment = this.getById(id);
        if (comment != null) {
            comment.setStatus(2); // 设置为已删除状态
            return this.updateById(comment);
        }
        return false;
    }

}
