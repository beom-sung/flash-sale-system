package com.commerce.flashsale.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String key, Object value) {
        redisTemplate.opsForValue()
            .set(key, value);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue()
            .get(key);
    }
}
