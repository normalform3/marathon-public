package org.example.marathonservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

//手动序列化 工具类
public class RedisUtil {
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        // 注册 JavaTimeModule 以支持 Java 8 日期时间类型
        mapper.registerModule(new JavaTimeModule());
    }

    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 将对象序列化为 JSON 字符串并存储到 Redis 中
     * @param key Redis 键
     * @param value 要存储的对象
     * @param <T> 对象类型
     */
    public <T> void setObject(String key, T value) {
        try {
            String json = mapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * 将对象序列化为 JSON 字符串并存储到 Redis 中，同时设置过期时间
     * @param key Redis 键
     * @param value 要存储的对象
     * @param timeout 过期时间
     * @param unit 时间单位
     * @param <T> 对象类型
     */
    public <T> void setObject(String key, T value, long timeout, TimeUnit unit) {
        try {
            String json = mapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, timeout, unit);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * 从 Redis 中获取 JSON 字符串并反序列化为对象
     * @param key Redis 键
     * @param clazz 对象类型的 Class
     * @param <T> 对象类型
     * @return 反序列化后的对象
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }
}
