package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.RaceDO;
import org.example.marathondal.mapper.RaceMapper;
import org.example.marathonservice.enums.RaceStatus;
import org.example.marathonservice.enums.RegistrationMode;
import org.example.marathonservice.param.RaceParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RaceService extends ServiceImpl<RaceMapper, RaceDO> {
    @Autowired
    private RaceMapper raceMapper;

    @Autowired
    private RaceCacheService raceCacheService;

    //新增赛事
    public boolean addRace(RaceDO raceDO) {
        if (raceDO.getRaceStatus() == null) {
            raceDO.setRaceStatus(RaceStatus.NOT_STARTED.getCode());
        }
        if (raceDO.getRegistrationMode() == null) {
            raceDO.setRegistrationMode(RegistrationMode.LOTTERY.getCode());
        }
        raceMapper.insert(raceDO);
        raceCacheService.cacheRace(raceDO);
        return true;
    }

    //查询赛事列表
    public Page<RaceDO> getRacePage(RaceParam param) {
        // 获取当前页码，如果未传入则默认为 1
        int current = param.getCurrentPage() == null ? 1 : param.getCurrentPage();
        // 获取每页记录数，如果未传入则默认为 10
        int size = param.getPageSize() == null ? 10 : param.getPageSize();

        // 创建 Page 对象，指定当前页码和每页记录数
        Page<RaceDO> page = new Page<>(current, size);

        // 创建 QueryWrapper 对象
        QueryWrapper<RaceDO> queryWrapper = new QueryWrapper<>();

        // 根据 RaceParam 中的属性构建查询条件
        if (param.getRaceName() != null && !param.getRaceName().isEmpty()) {
            queryWrapper.like("race_name", param.getRaceName());
        }
        if (param.getRaceType() != null) {
            queryWrapper.eq("race_type", param.getRaceType());
        }
        if (param.getRaceId() != null && !param.getRaceId().isEmpty()) {
            queryWrapper.eq("race_id", param.getRaceId());
        }
        if (param.getEnrollStart() != null) {
            queryWrapper.ge("enroll_start", param.getEnrollStart());
        }
        if (param.getEnrollEnd() != null) {
            queryWrapper.le("enroll_end", param.getEnrollEnd());
        }
        if (param.getLocation() != null && !param.getLocation().isEmpty()) {
            queryWrapper.like("location", param.getLocation());
        }
        if (param.getRaceDate() != null) {
            queryWrapper.ge("race_date", param.getRaceDate());
        }
        if (param.getRaceStatus() != null) {
            queryWrapper.eq("race_status", param.getRaceStatus());
        }

        // 添加排序条件
        queryWrapper.orderByDesc("race_status = 2") // 报名中排在前面
                .orderByDesc("race_status = 1") // 未开始排在第二
                .orderByAsc("race_date") // 同类型赛事根据赛事时间排序，越早的越前
        ;

        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    // 根据id查询赛事信息
    public RaceDO getRaceById(Long id) {
        return raceCacheService.getRace(id);
    }

    //修改赛事信息
    @Transactional
    public Boolean updateRace(RaceDO raceDO) {
        // 1. 检查参数合法性
        if (raceDO == null || raceDO.getId() == null) {
            return false;
        }
        if (raceDO.getRegistrationMode() == null) {
            raceDO.setRegistrationMode(RegistrationMode.LOTTERY.getCode());
        }

        // 2. 检查日期逻辑
        LocalDateTime enrollStart = raceDO.getEnrollStart();
        LocalDateTime enrollEnd = raceDO.getEnrollEnd();
        LocalDateTime raceDate = raceDO.getRaceDate();

        if (enrollStart != null && enrollEnd != null && enrollEnd.isBefore(enrollStart)) {
            throw new IllegalArgumentException("报名结束时间不能早于报名开始时间");
        }

        if (raceDate != null && enrollEnd != null && raceDate.isBefore(enrollEnd)) {
            throw new IllegalArgumentException("赛事开始时间不能早于报名结束时间");
        }

        // 3. 执行更新操作
        int rows = raceMapper.updateById(raceDO);
        if (rows > 0) {
            raceCacheService.evictRace(raceDO.getId());
            RaceDO updated = raceMapper.selectById(raceDO.getId());
            raceCacheService.cacheRace(updated);
        }

        // 4. 返回结果
        return rows > 0;
    }
}
