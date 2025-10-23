
package com.example.DATN.dtos.respone;

import com.example.DATN.constant.Is_Available;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String slug;
    private String productCode;
    private String description;
    private Is_Available available;
    private List<ProductColorResponse> colorResponses;
    private LocalDate createdAt;
    private LocalDate updatedAt;

}
