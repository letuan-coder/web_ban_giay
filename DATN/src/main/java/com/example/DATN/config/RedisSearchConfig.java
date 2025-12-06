package com.example.DATN.config;
import com.redislabs.lettusearch.RediSearchClient;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import io.lettuce.core.RedisURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisSearchConfig {
    @Bean
    public StatefulRediSearchConnection<String, String> redisSearchConnection() {
        // Kết nối Redis trên localhost:6379
        RediSearchClient client = RediSearchClient.create(RedisURI.create("redis://localhost:6379"));
        return client.connect(); // trả về connection để dùng
    }
}
