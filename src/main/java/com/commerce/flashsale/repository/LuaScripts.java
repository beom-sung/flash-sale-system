package com.commerce.flashsale.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@UtilityClass
public class LuaScripts {

    public static final DefaultRedisScript<Boolean> STOCK_DECREMENT_SCRIPT = new DefaultRedisScript<>(
        """
             local current = redis.call('GET', KEYS[1])
             local alreadyOrdered = redis.call('EXISTS', KEYS[2])
            
             if current and tonumber(current) > 0 and alreadyOrdered == 0 then
                 redis.call('DECR', KEYS[1])
                 redis.call('SET', KEYS[2], 1)
                 return 1
             else
                 return 0
             end
            """,
        Boolean.class
    );
}
