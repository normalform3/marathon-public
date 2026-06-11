package org.example.marathonwebapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.marathondal.entity.RaceDO;
import org.example.marathonservice.param.RaceParam;
import org.example.marathonservice.service.RaceService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/race")
public class RaceController {
    @Autowired
    private RaceService raceService;

    //查询赛事列表
    @PostMapping("/list")
    public WebResult<Page<RaceDO>> list(@RequestBody RaceParam param) {
        return WebResult.success(raceService.getRacePage(param));
    }

    //查询赛事详情
    @GetMapping("/detail")
    public WebResult<RaceDO> detail(@RequestParam Long id) {
        RaceDO race = raceService.getRaceById(id);
        if (race == null) {
            return WebResult.fail("赛事不存在");
        } else {
            return WebResult.success(race);
        }
    }

    //新增赛事
    @PostMapping("/add")
    public WebResult<String> add(@RequestBody RaceDO raceDO) {
        if(raceService.addRace(raceDO)){
            return WebResult.success("新增成功");
        }
        return WebResult.fail("新增失败");
    }

    //修改赛事信息
    @PostMapping("/update")
    public WebResult<String> update(@RequestBody RaceDO raceDO) {
        if(raceService.updateRace(raceDO)){
            return WebResult.success("修改成功");
        }
        return WebResult.fail("修改失败");
    }
}
