package com.example.DATN.dtos.respone.product;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.respone.ProductReviewResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailReponse {
    private UUID id;
    private String name;
    private String slug;
    private BigDecimal price;
    private String productCode;
    private String description;
    private Is_Available available;
    private String thumbnailUrl;
    private Long totalView;
    private Double averageRating;
    private Integer totalComment;    
    private List<ProductColorDetailResponse> colorResponses;
    private List<ProductVariantDetailResponse> variantDetailResponses;
    private List<ProductReviewResponse> reviewResponses;
}
