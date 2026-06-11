package org.example.marathonservice.scripts;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationLuaScriptTest {

    @Test
    void registerScriptKeepsCriticalSectionAtomic() throws IOException {
        String script = new String(
                getClass().getClassLoader().getResourceAsStream("scripts/register.lua").readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertTrue(script.contains("HGET"));
        assertTrue(script.contains("registrationMode"));
        assertTrue(script.contains("registrationMode == nil or registrationMode == 1"));
        assertTrue(script.contains("DECR"));
        assertTrue(script.contains("SET"));
        assertTrue(script.contains("return 4"));
        assertTrue(script.contains("return 5"));
    }
}
