package com.example.DATN.repositories;

import com.example.DATN.models.ProductVariantIndex;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariantIndexRepository
        extends ElasticsearchRepository<ProductVariantIndex, UUID> {
    List<ProductVariantIndex> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<ProductVariantIndex> findBySkuContainingIgnoreCase(String sku);
}
