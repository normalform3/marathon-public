package org.example.marathonservice.enums;

public enum RegistrationLuaResult {
    SUCCESS(0, "报名已受理"),
    RACE_NOT_FOUND(1, "赛事不存在"),
    NOT_STARTED(2, "报名未开始"),
    ENDED(3, "报名已结束"),
    ALREADY_REGISTERED(4, "您已报名该赛事"),
    SOLD_OUT(5, "名额不足"),
    SYSTEM_BUSY(6, "系统繁忙，请稍后再试"),
    INVALID_PARAM(7, "报名参数不完整"),
    HEALTH_REQUIRED(8, "请先完成健康评估"),
    HEALTH_NOT_QUALIFIED(9, "健康资格不符合该赛事要求"),
    RACE_NOT_ENROLLING(10, "赛事当前不可报名");

    private final int code;
    private final String message;

    RegistrationLuaResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static RegistrationLuaResult fromCode(long code) {
        for (RegistrationLuaResult result : values()) {
            if (result.code == code) {
                return result;
            }
        }
        return SYSTEM_BUSY;
    }
}
