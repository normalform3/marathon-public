package org.example.marathonservice.enums;

public enum RegistrationMode {
    LOTTERY(1, "超额报名后抽签"),
    FIRST_COME(2, "限额报名，报完即止");

    private final int code;
    private final String description;

    RegistrationMode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RegistrationMode fromCode(Integer code) {
        if (code == null) {
            return LOTTERY;
        }
        for (RegistrationMode mode : values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        return LOTTERY;
    }
}
