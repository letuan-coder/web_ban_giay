package com.example.DATN.repositories.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public interface ProductSalesProjection {
    UUID getProductId();
    String getProductName();
    String getProductCode();
    String getThumbnailUrl();
    BigDecimal getPrice();
    Long getTotalSold();
    Long getTotalStock();
    BigDecimal getTotalRevenue();
    LocalDateTime getCreatedAt();
}
