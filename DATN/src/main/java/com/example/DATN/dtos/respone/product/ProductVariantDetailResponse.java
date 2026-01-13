package com.example.DATN.dtos.respone.product;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.respone.StockResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ProductVariantDetailResponse {
    private UUID id;
    private String sku;
    private String size;
    private String colorName;
    private String colorHex;
    private Is_Available isAvailable;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private Integer total_stock;
    @JsonIgnore
    private List<StockResponse> stocks;
    public void calculateAndSetTotalStock() {
        if (this.stocks != null) {
            this.total_stock = this.stocks.stream()
                                  .mapToInt(StockResponse::getQuantity)
                                  .sum();
        } else {
            this.total_stock = 0;
        }
    }

}


