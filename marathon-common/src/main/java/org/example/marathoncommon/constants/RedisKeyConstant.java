package org.example.marathoncommon.constants;

public final class RedisKeyConstant {
    public static final String RACE_INFO_KEY = "race:info:";
    public static final String RACE_META_KEY = "race:%d:meta";
    public static final String RACE_STOCK_KEY = "race:%d:stock";
    public static final String RACE_USER_MARK_KEY = "race:%d:user:%d";
    public static final String RACE_NUMBER_KEY = "race:number:";
    public static final String REGISTRATION_IDEMPOTENT_KEY = "registration:msg:";

    private RedisKeyConstant() {
    }
}
