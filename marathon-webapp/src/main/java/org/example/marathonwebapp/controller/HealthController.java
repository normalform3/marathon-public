package org.example.marathonwebapp.controller;

import org.example.marathonservice.param.HealthAuditParam;
import org.example.marathonservice.param.ReSubmitParam;
import org.example.marathonservice.service.HealthService;
import org.example.marathonwebapp.model.WebResult;
import org.example.marathonwebapp.utils.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/health")
public class HealthController {
    @Autowired
    private HealthService healthService;

    @Autowired
    private MinioUtil minioUtil;


    //提交健康评估
    @PostMapping("/audit")
    public WebResult<String> audit(@RequestBody HealthAuditParam param) {
        int res = healthService.audit(param);
        if (res == 1) {
            return WebResult.fail("请勿重复提交");
        } else if (res == 2) {
            return WebResult.success("健康评估提交成功");
        }
        return WebResult.fail("健康评估提交失败");
    }

    //重新提交材料
    @PostMapping("/resubmit")
    public WebResult<String> reSubmit(@RequestBody ReSubmitParam param) {
        healthService.reSubmit(param);
        return WebResult.success("重新提交成功");
    }

    // 文件上传 返回文件的访问 URL
    @PostMapping("/upload")
    public WebResult<String> upload(@RequestBody MultipartFile file) {
        try {
            String bucketName = "marathon";
            String fileUrl = minioUtil.uploadFile(bucketName, file);
            return WebResult.success(fileUrl);
        } catch (Exception e) {
            return WebResult.fail("文件上传失败: " + e.getMessage());
        }
    }

    // 根据用户id查询全部健康档案
    @GetMapping("/getDocsByUserId")
    public WebResult getDocsByUserId(@RequestParam Long userId) {
        return WebResult.success(healthService.getHealthByUserId(userId));
    }

    //检查用户是否有已提交的审核，或者已审核且在有效期的记录
    @GetMapping("/checkAuditStatus")
    public WebResult checkAuditStatus(@RequestParam Long userId) {
        return WebResult.success(healthService.checkAuditStatus(userId));
    }

    //查询全部健康档案
    @GetMapping("/getAllDocs")
    public WebResult getAllDocs() {
        return WebResult.success(healthService.getAllDocs());
    }

    //通过审核
    @GetMapping("/pass")
    public WebResult pass(@RequestParam Long docId) {
        healthService.pass(docId);
        return WebResult.success();
    }

    //不通过审核 材料有误
    @GetMapping("/notPass")
    public WebResult notPass(@RequestParam Long docId) {
        healthService.notPass(docId);
        return WebResult.success();
    }

    //体检单不合格
    @GetMapping("/notQualified")
    public WebResult notQualified(@RequestParam Long docId) {
        healthService.notQualified(docId);
        return WebResult.success();
    }

    //状态设为待定
    @GetMapping("/setPending")
    public WebResult setPending(@RequestParam Long docId) {
        healthService.setPending(docId);
        return WebResult.success();
    }

}
