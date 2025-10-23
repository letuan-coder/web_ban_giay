package com.example.DATN.config;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(1, 1, 1672531200000L);
    }
}
