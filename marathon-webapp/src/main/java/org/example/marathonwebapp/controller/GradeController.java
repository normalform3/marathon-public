package org.example.marathonwebapp.controller;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import org.example.marathonservice.VO.AthleteExcel;
import org.example.marathonservice.VO.GradeVO;
import org.example.marathonservice.VO.UserVO;
import org.example.marathonservice.service.GradeService;
import org.example.marathonservice.service.RaceService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/grade")
public class GradeController {
    @Autowired
    private GradeService gradeService;
    @Autowired
    private RaceService raceService;

    //查询用户成绩
    @GetMapping("/list")
    public WebResult<List<GradeVO>> list(@RequestParam("userId") Long id) {
        List<GradeVO> gradeVOList = gradeService.list(id);
        return WebResult.success(gradeVOList);
    }

    //下载成绩模板
    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response,@RequestParam("raceId") Long raceId) throws IOException {
        // excel数据来源
        List<AthleteExcel> athletes = gradeService.getAthleteList(raceId); // 你的数据源

        // 设置响应内容类型为excel
        response.setContentType("application/vnd.ms-excel");
        // 设置编码
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=score_template.xlsx");

        // 写入响应体
        EasyExcel.write(response.getOutputStream(), AthleteExcel.class)
                .sheet("成绩模板")
                .doWrite(athletes);
    }

    //上传成绩
    @PostMapping("/upload-grades")
    public WebResult<String> uploadScores(@RequestParam("file") MultipartFile file,@RequestParam("raceId") Long raceId) throws IOException {
        List<AthleteExcel> list = EasyExcel.read(file.getInputStream())
                .head(AthleteExcel.class)
                .sheet()
                .doReadSync();
        try {
            gradeService.saveGrades(list,raceId);
        } catch (Exception e) {
            return WebResult.fail("成绩上传失败");
        }
        return WebResult.success("成绩上传成功");
    }

    //查询赛事状态，决定功能是否开启
    @GetMapping("/race-status")
    public WebResult<Integer> getRaceStatus(@RequestParam("raceId") Long raceId) {
        Integer status = raceService.getRaceById(raceId).getRaceStatus();
        if (status == null) {
            return WebResult.fail("赛事不存在");
        }
        return WebResult.success(status);
    }
}
