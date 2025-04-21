package com.commerce.flashsale.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisRepository {

    private static final String ORDER_COUNT = "order_count";

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Boolean> orderDecrementScript;

    public boolean reduceOrderCount() {
        Boolean execute = redisTemplate.execute(
            orderDecrementScript,
            List.of(ORDER_COUNT)
        );

        return Boolean.TRUE.equals(execute);
    }
}
