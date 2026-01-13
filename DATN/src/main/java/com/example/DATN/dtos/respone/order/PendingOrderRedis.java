package com.example.DATN.dtos.respone.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PendingOrderRedis {
    private String orderCode;
    private String Note;
    private Long userId;
    private ShippingAddressRedis userAddresses;
    private List<PendingOrderItem> items;
    private BigDecimal totalPrice;
    private Integer totalWeight;
    private Integer totalHeight;
    private Integer totalWidth;
    private Integer totalLength;
}
