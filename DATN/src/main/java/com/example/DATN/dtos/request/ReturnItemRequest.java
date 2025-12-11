package com.example.DATN.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemRequest {
    private UUID orderItemId;
    private Integer quantity;
}
