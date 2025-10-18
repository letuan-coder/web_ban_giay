package com.example.DATN.dtos.request.ghtk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GHTKRequest {
    private List<GhtkProduct> products;
    private GhtkOrderInfo order;
}
