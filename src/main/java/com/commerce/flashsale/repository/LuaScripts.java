package com.commerce.flashsale.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class LuaScripts {

    @Bean
    public DefaultRedisScript<Boolean> orderDecrementScript() {
        String script = """
            local current = redis.call('get', KEYS[1])
            if current and tonumber(current) > 0 then
                redis.call('decr', KEYS[1])
                return 1
            else
                return 0
            end
            """;
        return new DefaultRedisScript<>(script, Boolean.class);
    }

}
