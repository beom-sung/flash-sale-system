package com.commerce.flashsale.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.commerce.flashsale.repository.LuaScripts.ORDER_DECREMENT_SCRIPT;

@Component
@RequiredArgsConstructor
public class RedisRepository {

    private static final String ORDER_COUNT = "order_count";

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean reduceOrderCount() {
        Boolean execute = redisTemplate.execute(
            ORDER_DECREMENT_SCRIPT,
            List.of(ORDER_COUNT)
        );

        return Boolean.TRUE.equals(execute);
    }
}
