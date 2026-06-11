package org.example.marathonwebapp.controller;

import org.example.marathonservice.VO.AthleteVO;
import org.example.marathonservice.VO.RegistResultVO;
import org.example.marathonservice.enums.RegistrationLuaResult;
import org.example.marathonservice.param.RegistrationParam;
import org.example.marathonservice.service.RegistrationService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registration")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;

    //用户报名(提交用户 赛事id)
    @PostMapping("/add")
    public WebResult<String> add(@RequestBody RegistrationParam param) {
        RegistrationLuaResult result = registrationService.addRegistration(param);
        if (result == RegistrationLuaResult.SUCCESS) {
            return WebResult.success(result.getMessage());
        }
        return WebResult.fail(result.getMessage());
    }

    //是否已报名
    @PostMapping("/isRegister")
    public WebResult<Boolean> isRegistered(@RequestBody RegistrationParam param) {
        return WebResult.success(registrationService.isRegistered(param));
    }

    //查看用户全部报名
    @PostMapping("/list")
    public WebResult<List<RegistResultVO>> list(@RequestBody RegistrationParam param) {
        return WebResult.success(registrationService.list(param.getUserId()));
    }

    //赛事抽签
    @PostMapping("/draw")
    public WebResult<String> draw(@RequestParam("raceId") Long raceId) {
        int res = registrationService.lottery(raceId);
        if(res == 1) {
            return WebResult.fail("比赛报名未结束，无法抽签");
        } else if (res == 2) {
            return WebResult.fail("赛事不存在");
        } else if (res == 3) {
            return WebResult.fail("先到先得赛事不需要抽签");
        } else {
            return WebResult.success("抽签成功");
        }
    }

    //查看报名人数
    @GetMapping("/count")
    public WebResult<Long> count(@RequestParam("raceId") Long raceId) {
        return WebResult.success(registrationService.count(raceId));
    }

    //查看某赛事运动员列表
    @GetMapping("/athleteList")
    public WebResult<List<AthleteVO>> getAthletesByRaceId(@RequestParam("raceId") Long raceId) {
        List<AthleteVO> athleteList = registrationService.getAthletesByRaceId(raceId);
        return WebResult.success(athleteList);
    }
}
