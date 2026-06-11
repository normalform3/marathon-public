package org.example.marathonservice.param;

import lombok.Data;

@Data
public class RegistrationParam extends BasePageParam{
    //赛事id，不是赛事编号
    Long raceId;

    //用户id
    Long userId;

    //报名状态
    Integer status;
}
