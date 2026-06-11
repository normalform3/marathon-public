package org.example.marathonservice.VO;

import lombok.Data;

@Data
public class UserVO {
    // 用户id
    private Long id;

    // 账号
    private String account;

    // 用户名
    private String name;

    // 身份
    private Integer type;

    // token
    private String token;

    // 赛事id（仅举办方有）
    private Long raceId;
}
