package com.example.DATN.dtos.request.product;

import com.example.DATN.constant.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {
    private String name;
    private String description;
    private PromotionType promotionType;
    private Double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private List<UUID> productVariantIds;
}