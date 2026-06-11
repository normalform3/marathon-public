package org.example.marathonwebapp.controller;

import org.example.marathondal.entity.CommentDO;
import org.example.marathonservice.param.CommentParam;
import org.example.marathonservice.service.CommentService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    // 用户提交评论
    @PostMapping("/submit")
    public WebResult<String> submit(@RequestBody CommentParam param) {
        if(commentService.submit(param)){
            return WebResult.success("评论提交成功");
        } else {
            return WebResult.fail("评论提交失败");
        }
    }

    // 根据id查看某条评论
    @GetMapping("/getById")
    public WebResult<CommentDO> getById(@RequestParam("id") Long id) {
        CommentDO comment = commentService.getById(id);
        if (comment != null) {
            return WebResult.success(comment);
        } else {
            return WebResult.fail("评论不存在");
        }
    }

    // 根据赛事id查看评论
    @GetMapping("/getByRaceId")
    public WebResult<List<CommentDO>> getByRaceId(@RequestParam("raceId") Long raceId) {
        List<CommentDO> comments = commentService.getByRaceId(raceId);
        if (comments != null && !comments.isEmpty()) {
            return WebResult.success(comments);
        } else {
            return WebResult.fail("没有评论");
        }
    }

    //管理员查看全部评论
    @GetMapping("/getAll")
    public WebResult<List<CommentDO>> getAll() {
        List<CommentDO> comments = commentService.list();

        if (comments != null && !comments.isEmpty()) {
            // 按照评论时间降序排列 从新的到旧的
            comments.sort((c1, c2) -> c2.getCommentTime().compareTo(c1.getCommentTime()));
            return WebResult.success(comments);
        } else {
            return WebResult.fail("没有评论");
        }
    }

    // 根据赛事id 通过/恢复评论
    @GetMapping("/passById")
    public WebResult<String> passById(@RequestParam("id") Long id) {
        CommentDO comment = commentService.getById(id);
        if (comment != null) {
            comment.setStatus(1); // 设置为已通过状态
            commentService.updateById(comment);
            return WebResult.success("评论通过成功");
        } else {
            return WebResult.fail("评论不存在");
        }
    }

    // 根据id 逻辑删除/不通过评论
    @GetMapping("/deleteById")
    public WebResult<String> deleteById(@RequestParam("id") Long id) {
        if (commentService.logicDelete(id)) {
            return WebResult.success("评论删除成功");
        } else {
            return WebResult.fail("评论删除失败");
        }
    }


}
