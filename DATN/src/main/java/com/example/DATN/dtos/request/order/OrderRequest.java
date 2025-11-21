package com.example.DATN.dtos.request.order;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderRequest {
    private List<UUID> productColorId;
    private Long paymentMethodId;
}