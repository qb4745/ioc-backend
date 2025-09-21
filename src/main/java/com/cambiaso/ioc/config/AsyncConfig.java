package com.cambiaso.ioc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${app.etl.executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${app.etl.executor.max-pool-size:4}")
    private int maxPoolSize;

    @Value("${app.etl.executor.queue-capacity:200}")
    private int queueCapacity;

    @Bean(name = "etlExecutor")
    public Executor etlExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("ETL-");
        executor.initialize();
        return executor;
    }
}
