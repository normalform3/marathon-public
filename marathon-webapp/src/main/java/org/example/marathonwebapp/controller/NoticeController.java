package org.example.marathonwebapp.controller;

import org.example.marathondal.entity.NoticeDO;
import org.example.marathonservice.VO.NoticeVO;
import org.example.marathonservice.service.NoticeService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    // 根据用户ID获取通知
    @GetMapping("/getNotice")
    public WebResult<List<NoticeVO>> getNotice(Long userId) {
        return WebResult.success(noticeService.getNotice(userId));
    }

    // 根据订阅id将通知设为已读
    @GetMapping("/setRead")
    public WebResult<String> setRead(Long subscribeId) {
        noticeService.setRead(subscribeId);
        return WebResult.success("设置已读成功");
    }
}
