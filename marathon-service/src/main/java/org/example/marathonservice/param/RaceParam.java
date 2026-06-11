package org.example.marathonservice.param;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RaceParam extends BasePageParam{
    String raceName;

    //赛事类型 1全马 2半马 3:10公里 4:5公里
    Integer raceType;

    //赛事编号
    String raceId;
    LocalDateTime enrollStart;
    LocalDateTime enrollEnd;
    String location;
    LocalDateTime raceDate;

    //赛事状态 1:未开始 2:进行中 3:已结束
    Integer raceStatus;
}
