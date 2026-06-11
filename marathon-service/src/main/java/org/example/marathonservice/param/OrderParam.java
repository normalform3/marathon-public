package org.example.marathonservice.param;

import lombok.Data;

@Data
public class OrderParam {
    //订单id
    Long id;

    //赛事id
    Long raceId;

    //用户id
    Long userId;
}
