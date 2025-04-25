package com.commerce.flashsale.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.commerce.flashsale.repository.LuaScripts.STOCK_DECREMENT_SCRIPT;

@Component
@RequiredArgsConstructor
public class RedisRepository {

    private static final String KEY_PREFIX = "order_count_";

    private final RedisTemplate<String, Object> redisTemplate;

    public void setProduct(String productName, int stockCount) {
        redisTemplate.opsForValue().set(KEY_PREFIX + productName, stockCount);
    }

    public boolean reduceStockCount(String productName, String uuid) {
        Boolean execute = redisTemplate.execute(
            STOCK_DECREMENT_SCRIPT,
            List.of(KEY_PREFIX + productName, productName + "_" +uuid)
        );

        return Boolean.TRUE.equals(execute);
    }
}
