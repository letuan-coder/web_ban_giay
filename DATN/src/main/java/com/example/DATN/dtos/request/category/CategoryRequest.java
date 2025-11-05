
package com.example.DATN.dtos.request.category;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    @NotEmpty(message = "CATEGORY_NAME_REQUIRED")
    private String name;
    private String description;
}
