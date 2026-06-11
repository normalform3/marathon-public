package org.example.marathonservice.service;

import org.example.marathoncommon.constants.RedisKeyConstant;
import org.example.marathonservice.enums.RegistrationLuaResult;
import org.example.marathonservice.enums.RegistrationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RegistrationLuaExecutor {
    private static final long USER_MARK_TTL_SECONDS = 60L * 60 * 24 * 60;

    private final DefaultRedisScript<Long> registerScript;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public RegistrationLuaExecutor() {
        this.registerScript = new DefaultRedisScript<>();
        this.registerScript.setResultType(Long.class);
        this.registerScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/register.lua")));
    }

    public RegistrationLuaResult tryRegister(Long raceId, Long userId) {
        String metaKey = String.format(RedisKeyConstant.RACE_META_KEY, raceId);
        String stockKey = String.format(RedisKeyConstant.RACE_STOCK_KEY, raceId);
        String userMarkKey = String.format(RedisKeyConstant.RACE_USER_MARK_KEY, raceId, userId);
        Long code = stringRedisTemplate.execute(
                registerScript,
                List.of(metaKey, stockKey, userMarkKey),
                String.valueOf(Instant.now().getEpochSecond()),
                String.valueOf(USER_MARK_TTL_SECONDS)
        );
        return RegistrationLuaResult.fromCode(code == null ? RegistrationLuaResult.SYSTEM_BUSY.getCode() : code);
    }

    public void rollbackUserMarkAndStock(Long raceId, Long userId, Integer registrationMode) {
        stringRedisTemplate.delete(String.format(RedisKeyConstant.RACE_USER_MARK_KEY, raceId, userId));
        if (RegistrationMode.fromCode(registrationMode) == RegistrationMode.FIRST_COME) {
            stringRedisTemplate.opsForValue().increment(String.format(RedisKeyConstant.RACE_STOCK_KEY, raceId));
        }
    }
}
