package org.example.marathonservice.enums;

public enum RaceStatus {
    DRAFT(0, "草稿"),
    PENDING_REVIEW(1, "待审核"),
    NOT_STARTED(2, "报名未开始"),
    ENROLLING(3, "报名中"),
    ENROLL_CLOSED(4, "报名截止"),
    LOTTERY_COMPLETED(5, "抽签完成"),
    FINISHED(6, "比赛结束"),
    CANCELLED(7, "取消");

    private final int code;
    private final String description;

    RaceStatus(int code, String description) {
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
