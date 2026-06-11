package org.example.marathonservice.VO;

import lombok.Data;

@Data
public class RegistResultVO {
    // 赛事id
    Long raceId;

    // 赛事名称
    String raceName;

    // 赛事类型
    Integer raceType;

    //报名状态
    Integer status;

    //参赛号
    Integer athleteNumber;

}
