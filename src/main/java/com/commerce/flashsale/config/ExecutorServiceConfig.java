package com.commerce.flashsale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfig {

    @Bean(name = "taskExecutor")
    public ExecutorService taskExecutor() {
        return Executors.newFixedThreadPool(50); // 고정 크기 스레드 풀
    }
}
