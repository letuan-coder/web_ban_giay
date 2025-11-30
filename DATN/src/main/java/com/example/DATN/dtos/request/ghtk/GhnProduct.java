package com.example.DATN.dtos.request.ghtk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GhnProduct {
    private String name;
    private String code;
    private Integer quantity;
    private Long price;
    private Integer length;
    private Integer width;
    private Integer height;
    private Integer weight;
    private Map<String, String> category; // level1, level2...

}
