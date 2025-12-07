package com.example.DATN.models;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
@RedisHash("product")
public class ProductRedis {
    @Id
    private UUID id;


    @Searchable
    private String name;

    @Indexed
    private String slug;

    @Searchable

    private String productCode;

    private BigDecimal price;

    private String thumbnailUrl;
}
