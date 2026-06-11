package org.example.marathonservice.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistrationLuaResultTest {

    @Test
    void mapsLuaReturnCodesToBusinessResults() {
        assertEquals(RegistrationLuaResult.SUCCESS, RegistrationLuaResult.fromCode(0));
        assertEquals(RegistrationLuaResult.ALREADY_REGISTERED, RegistrationLuaResult.fromCode(4));
        assertEquals(RegistrationLuaResult.SOLD_OUT, RegistrationLuaResult.fromCode(5));
        assertEquals(RegistrationLuaResult.SYSTEM_BUSY, RegistrationLuaResult.fromCode(999));
    }

    @Test
    void registrationModeDefaultsToLotteryForOldData() {
        assertEquals(RegistrationMode.LOTTERY, RegistrationMode.fromCode(null));
        assertEquals(RegistrationMode.LOTTERY, RegistrationMode.fromCode(1));
        assertEquals(RegistrationMode.FIRST_COME, RegistrationMode.fromCode(2));
        assertEquals(RegistrationMode.LOTTERY, RegistrationMode.fromCode(999));
    }
}
