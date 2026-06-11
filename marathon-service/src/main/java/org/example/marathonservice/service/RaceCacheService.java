package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.marathoncommon.constants.RedisKeyConstant;
import org.example.marathondal.entity.RaceDO;
import org.example.marathondal.entity.RegistrationDO;
import org.example.marathondal.mapper.RaceMapper;
import org.example.marathondal.mapper.RegistrationMapper;
import org.example.marathonservice.enums.RegistrationMode;
import org.example.marathonservice.enums.RegistrationStatus;
import org.example.marathonservice.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class RaceCacheService {
    private static final int BASE_TTL_MINUTES = 10;
    private static final int NULL_TTL_SECONDS = 60;

    private final Cache<Long, RaceDO> localRaceCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();

    @Autowired
    private RaceMapper raceMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public RaceDO getRace(Long raceId) {
        if (raceId == null || raceId <= 0) {
            return null;
        }
        RaceDO local = localRaceCache.getIfPresent(raceId);
        if (local != null) {
            return local.getId() == null ? null : local;
        }

        RedisUtil redisUtil = new RedisUtil(stringRedisTemplate);
        String raceInfoKey = RedisKeyConstant.RACE_INFO_KEY + raceId;
        RaceDO cached = redisUtil.getObject(raceInfoKey, RaceDO.class);
        if (cached != null) {
            localRaceCache.put(raceId, cached);
            return cached.getId() == null ? null : cached;
        }

        RaceDO race = raceMapper.selectById(raceId);
        if (race == null) {
            RaceDO empty = new RaceDO();
            redisUtil.setObject(raceInfoKey, empty, NULL_TTL_SECONDS, TimeUnit.SECONDS);
            localRaceCache.put(raceId, empty);
            return null;
        }

        cacheRace(race);
        return race;
    }

    public void cacheRace(RaceDO race) {
        if (race == null || race.getId() == null) {
            return;
        }
        RedisUtil redisUtil = new RedisUtil(stringRedisTemplate);
        int jitter = ThreadLocalRandom.current().nextInt(0, 180);
        redisUtil.setObject(
                RedisKeyConstant.RACE_INFO_KEY + race.getId(),
                race,
                BASE_TTL_MINUTES * 60L + jitter,
                TimeUnit.SECONDS
        );
        localRaceCache.put(race.getId(), race);
        warmRegistrationKeys(race);
    }

    public void evictRace(Long raceId) {
        if (raceId == null) {
            return;
        }
        localRaceCache.invalidate(raceId);
        stringRedisTemplate.delete(RedisKeyConstant.RACE_INFO_KEY + raceId);
        stringRedisTemplate.delete(String.format(RedisKeyConstant.RACE_META_KEY, raceId));
        stringRedisTemplate.delete(String.format(RedisKeyConstant.RACE_STOCK_KEY, raceId));
    }

    public void warmRegistrationKeys(RaceDO race) {
        if (race == null || race.getId() == null || race.getParticipantNumber() == null) {
            return;
        }
        String metaKey = String.format(RedisKeyConstant.RACE_META_KEY, race.getId());
        String stockKey = String.format(RedisKeyConstant.RACE_STOCK_KEY, race.getId());

        Map<String, String> meta = new HashMap<>();
        meta.put("raceId", String.valueOf(race.getId()));
        meta.put("raceStatus", String.valueOf(race.getRaceStatus()));
        meta.put("registrationMode", String.valueOf(RegistrationMode.fromCode(race.getRegistrationMode()).getCode()));
        if (race.getEnrollStart() != null) {
            meta.put("enrollStart", String.valueOf(race.getEnrollStart().toEpochSecond(ZoneOffset.ofHours(8))));
        }
        if (race.getEnrollEnd() != null) {
            meta.put("enrollEnd", String.valueOf(race.getEnrollEnd().toEpochSecond(ZoneOffset.ofHours(8))));
        }
        stringRedisTemplate.opsForHash().putAll(metaKey, meta);
        if (RegistrationMode.fromCode(race.getRegistrationMode()) == RegistrationMode.FIRST_COME) {
            long registeredCount = registrationMapper.selectCount(new QueryWrapper<RegistrationDO>()
                    .eq("race_id", race.getId())
                    .ne("status", RegistrationStatus.CANCELLED.getCode()));
            long remainingStock = Math.max(0, race.getParticipantNumber() - registeredCount);
            stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(remainingStock));
        }
    }
}
