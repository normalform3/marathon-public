package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.RouteDO;
import org.example.marathondal.mapper.RouteMapper;
import org.example.marathonservice.utils.RedisIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RouteService extends ServiceImpl<RouteMapper, RouteDO> {
    @Autowired
    private RedisIdWorker redisIdWorker;

    //提交路线
    public boolean submitRoute(RouteDO route) {
        //检查赛事路线是否存在
        QueryWrapper<RouteDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("race_id", route.getRaceId());
        RouteDO existingRoute = this.getOne(queryWrapper);
        if (existingRoute != null) {
            //如果存在，则更新路线
            existingRoute.setRoute(route.getRoute());
            existingRoute.setCreateTime(LocalDateTime.now());
            return this.updateById(existingRoute);
        } else {
            //如果不存在，则创建新的路线
            route.setId(redisIdWorker.nextId("route"));
            route.setRoute(route.getRoute());
            route.setCreateTime(LocalDateTime.now());
            return this.save(route);
        }
    }

    //查询路线
    public RouteDO getByRaceId(Long raceId){
        QueryWrapper<RouteDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("race_id", raceId);
        return this.getOne(queryWrapper);
    }

    // 是否存在路线
    public boolean exist(Long raceId){
        QueryWrapper<RouteDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("race_id", raceId);
        RouteDO existingRoute = this.getOne(queryWrapper);
        return existingRoute != null;
    }
}
