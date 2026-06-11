package org.example.marathonservice.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {
    // 初始时间
    private final static long BEGIN_TIME = 1741651200L;
    // 序列号占用位数
    private final static int COUNT_BITS = 20;
    // 时间戳占用位数
    private final static int TIMESTAMP_BITS = 44;

    private StringRedisTemplate stringRedisTemplate;

    // 手动注入
    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 生成全局唯一ID
    // keyPrefix 为业务前缀, 用于区分不同业务
    public long nextId(String keyPrefix) {
        // 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIME;

        // 生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 拼接
        return (timeStamp & ((1L << TIMESTAMP_BITS) - 1)) << COUNT_BITS | (count & ((1L << COUNT_BITS) - 1));
    }
}