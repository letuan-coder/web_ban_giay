package com.example.DATN.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    final Integer CORE_POOL_SIZE = 4;
    final Integer QUEUE_CAPACITY = 150;
    final Integer MAX_POOL_SIZE = 4 ;
    @Bean(name = "asyncTaskExecutor")
    public Executor ayncTaskExcutor(){
        ThreadPoolTaskExecutor taskThreadPoolExecutor =new ThreadPoolTaskExecutor();
        taskThreadPoolExecutor.setCorePoolSize(CORE_POOL_SIZE);
        taskThreadPoolExecutor.setQueueCapacity(QUEUE_CAPACITY);
        taskThreadPoolExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        // Đặt tên prefix cho thread (giúp dễ debug)
        taskThreadPoolExecutor.setThreadNamePrefix("AsyncThread-");

        // Khởi tạo thread pool
        taskThreadPoolExecutor.initialize();

        return taskThreadPoolExecutor;
    }
}
