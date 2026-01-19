package com.example.DATN.dtos.respone.product;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.respone.SizeResponse;
import com.example.DATN.dtos.respone.StockResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {
    private UUID id;
    private SizeResponse size;
    private Is_Available isAvailable;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer total_stock;
    private String sku;
    @JsonIgnore
    private Set<StockResponse> stocks;
    private LocalDate createdAt;
    public Integer getTotal_stock() {
        if (stocks == null) return 0;
        return stocks.stream()
                .mapToInt(StockResponse::getQuantity)
                .sum();
    }
    public Is_Available getIsAvailable() {
        if(getTotal_stock()==0||getTotal_stock()==null)
            setIsAvailable(Is_Available.NOT_AVAILABLE);
        return isAvailable;
    }
}
