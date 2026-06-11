package org.example.marathonservice.param;

import lombok.Data;

@Data
public class UserParam {
    //用户id
    Long userId;

    //身份证号
    String identificationNumber;

    //手机号
    String phone;

    //紧急联系人手机号
    String emergencyPhone;
}
