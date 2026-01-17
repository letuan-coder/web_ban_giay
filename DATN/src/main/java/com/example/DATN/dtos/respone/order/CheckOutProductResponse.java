package com.example.DATN.dtos.respone.order;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.respone.StockResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutProductResponse {
    private UUID id;
    private Is_Available isAvailable;
    private Integer quantity;
    private String productName;
    private String sku;
    private String colorName;
    private Integer sizeName;
    private BigDecimal price;
    private BigDecimal finaPrice;
    private StockResponse stockResponse;
    private Integer stock;
    private String imageUrl;
    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;
}
