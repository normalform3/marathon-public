package org.example.marathonwebapp.controller;

import org.example.marathondal.entity.ApplyDO;
import org.example.marathonservice.service.ApplyService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/apply")
public class ApplyController {
    @Autowired
    private ApplyService applyService;

    //提交申请
    @PostMapping("/submit")
    public WebResult<String> submit(@RequestBody ApplyDO applyDO) {
        if (applyService.submit(applyDO)) {
            return WebResult.success("申请提交成功");
        }
        return WebResult.fail("申请提交失败");
    }

    //查看全部申请
    @GetMapping("/list")
    public WebResult<List<ApplyDO>> list() {
        return WebResult.success(applyService.getAllApply());
    }

    //通过申请
    @PostMapping("/approve")
    public WebResult<String> approve(@RequestParam("applyId") Long applyId) {
        if (applyService.approve(applyId)) {
            return WebResult.success("申请已通过");
        }
        return WebResult.fail("未查到申请");
    }

    //拒绝申请
    @PostMapping("/reject")
    public WebResult<String> reject(@RequestParam("applyId") Long applyId) {
        if (applyService.reject(applyId)) {
            return WebResult.success("申请已拒绝");
        }
        return WebResult.fail("未查到申请");
    }
}
