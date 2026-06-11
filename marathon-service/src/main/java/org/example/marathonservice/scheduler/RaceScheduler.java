package org.example.marathonservice.scheduler;

import org.example.marathondal.entity.HealthDO;
import org.example.marathondal.entity.RaceDO;
import org.example.marathondal.mapper.RaceMapper;
import org.example.marathonservice.enums.RaceStatus;
import org.example.marathonservice.service.RaceCacheService;
import org.example.marathonservice.service.HealthService;
import org.example.marathonservice.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RaceScheduler {
    @Autowired
    private RaceMapper raceMapper;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private HealthService healthService;

    @Autowired
    private RaceCacheService raceCacheService;



    //判断赛事状态
    private Integer determineRaceStatus(RaceDO race, LocalDateTime now) {
        if (race.getRaceStatus() != null && race.getRaceStatus() == RaceStatus.CANCELLED.getCode()) {
            return RaceStatus.CANCELLED.getCode();
        }
        if (now.isBefore(race.getEnrollStart())) {
            return RaceStatus.NOT_STARTED.getCode();
        } else if (now.isAfter(race.getEnrollStart()) && now.isBefore(race.getEnrollEnd())) {
            return RaceStatus.ENROLLING.getCode();
        } else if (now.isAfter(race.getEnrollEnd()) && now.isBefore(race.getRaceDate())) {
            return RaceStatus.ENROLL_CLOSED.getCode();
        } else if (now.isAfter(race.getRaceDate())) {
            return RaceStatus.FINISHED.getCode();
        }
        return race.getRaceStatus();
    }

    //定时更新赛事状态 每小时执行一次
    @Scheduled(cron = "0 0 * * * ?")
    public void updateRaceStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<RaceDO> races = raceMapper.selectList(null);

        for (RaceDO race : races) {
            Integer newStatus = determineRaceStatus(race, now);
            if (!newStatus.equals(race.getRaceStatus())) {
                race.setRaceStatus(newStatus);
                raceMapper.updateById(race);
                raceCacheService.evictRace(race.getId());
                raceCacheService.cacheRace(race);
                System.out.println("赛事 " + race.getRaceName() + " 状态更新为 " + newStatus);

                //如果赛事状态更新为报名结束，进行抽签
                if (newStatus == RaceStatus.ENROLL_CLOSED.getCode()){
                    registrationService.lottery(race.getId());
                    System.out.println(race.getRaceName() + "抽签完成");
                }
            }
        }
    }

    //定时更新健康状态 每天下午2点执行
    @Scheduled(cron = "0 0 14 * * ?")
    public void updateHealthStatus(){
        //查询所有超出有效期限的健康评估
        List<HealthDO> healths = healthService.getExpiredHealths();
        for(HealthDO health : healths){
            //更新健康状态
            healthService.updateHealthStatus(health.getUserId(), 0);
            //逻辑删除
            healthService.updateDocStatus(health.getId(),3);
        }
    }

}
