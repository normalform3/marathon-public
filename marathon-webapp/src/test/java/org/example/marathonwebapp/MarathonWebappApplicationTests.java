package org.example.marathonwebapp;

import org.example.marathonwebapp.utils.Jwt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarathonWebappApplicationTests {

    @Test
    void jwtContainsUserRoleAndUserId() {
        String token = Jwt.createToken("runner", 1001L, 1);

        assertTrue(Jwt.checkToken(token));
        assertEquals(1, Jwt.parseType(token));
        assertEquals(1001L, Jwt.parseUserId(token));
    }
}
