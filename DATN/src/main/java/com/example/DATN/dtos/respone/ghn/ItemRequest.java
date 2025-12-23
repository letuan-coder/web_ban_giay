package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemRequest {
    private String name;
    private int quantity;
    private int height;
    private int weight;
    private int length;
    private int width;
}
