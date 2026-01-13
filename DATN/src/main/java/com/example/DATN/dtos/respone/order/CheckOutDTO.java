package com.example.DATN.dtos.respone.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutDTO {
    private boolean isStore;
    private UUID storeId;
    private Double distanceKm;
}
