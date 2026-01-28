package com.example.DATN.dtos.request;

import lombok.Data;

import java.util.UUID;

@Data
public class MissingItemDTO {
    private UUID productVariantId;
    private Integer quantity;
}
