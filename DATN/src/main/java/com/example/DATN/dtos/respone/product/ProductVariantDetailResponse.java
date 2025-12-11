package com.example.DATN.dtos.respone.product;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.respone.ProductReviewResponse;
import com.example.DATN.dtos.respone.StockResponse;
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
    private Integer total_stock;
    private List<StockResponse> stocks;
    // Helper method to calculate and set total_stock from the 'stocks' list
    public void calculateAndSetTotalStock() {
        if (this.stocks != null) {
            this.total_stock = this.stocks.stream()
                                  .mapToInt(StockResponse::getQuantity) // Assumes StockResponse has getQuantity()
                                  .sum();
        } else {
            this.total_stock = 0;
        }
    }
}


