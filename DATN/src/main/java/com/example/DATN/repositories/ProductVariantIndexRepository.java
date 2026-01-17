package com.example.DATN.repositories;

import com.example.DATN.models.ProductVariantIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.UUID;

public interface ProductVariantIndexRepository
        extends ElasticsearchRepository<ProductVariantIndex, UUID> {

}
