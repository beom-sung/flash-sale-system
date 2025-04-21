package com.commerce.flashsale.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@UtilityClass
public class LuaScripts {

    public static final DefaultRedisScript<Boolean> ORDER_DECREMENT_SCRIPT = new DefaultRedisScript<>(
        """
            local current = redis.call('get', KEYS[1])
            if current and tonumber(current) > 0 then
                redis.call('decr', KEYS[1])
                return 1
            else
                return 0
            end
            """,
        Boolean.class
    );
}
