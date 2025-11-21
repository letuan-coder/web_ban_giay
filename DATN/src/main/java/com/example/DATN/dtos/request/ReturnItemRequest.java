package com.example.DATN.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemRequest {
    private Long orderItemId;
    private Integer quantity;
}
