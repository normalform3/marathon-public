package org.example.marathonservice.enums;

public enum RegistrationStatus {
    ACCEPTED(0, "受理中"),
    REGISTERED(1, "已报名"),
    CANCELLED(2, "已取消"),
    LOTTERY_LOST(3, "未中签"),
    LOTTERY_WON_PENDING_PAY(4, "中签待支付"),
    PAID(5, "已支付"),
    PAY_TIMEOUT(6, "支付超时");

    private final int code;
    private final String description;

    RegistrationStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
