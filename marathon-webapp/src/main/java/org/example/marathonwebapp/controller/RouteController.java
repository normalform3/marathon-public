package org.example.marathonwebapp.controller;

import org.example.marathondal.entity.RouteDO;
import org.example.marathonservice.service.RouteService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/route")
public class RouteController {
    @Autowired
    private RouteService routeService;

    // 提交路线
    @PostMapping("/submit")
    public WebResult<String> submitRoute(@RequestBody RouteDO route) {
        if(routeService.submitRoute(route)){
            return WebResult.success("路线提交成功");
        } else {
            return WebResult.fail("路线提交失败");
        }
    }

    //查询路线
    @GetMapping("/get")
    public WebResult<RouteDO> get(@RequestParam("raceId") Long raceId) {
        RouteDO route = routeService.getByRaceId(raceId);
        if(route != null){
            return WebResult.success(route);
        } else {
            return WebResult.fail("路线不存在");
        }
    }

    // 是否存在路线
    @GetMapping("/exist")
    public WebResult<String> exist(@RequestParam("raceId") Long raceId) {
        if(routeService.exist(raceId)){
            return WebResult.success("路线已存在");
        } else {
            return WebResult.fail("路线不存在");
        }
    }


}
