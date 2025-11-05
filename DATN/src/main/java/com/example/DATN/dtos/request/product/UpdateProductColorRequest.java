package com.example.DATN.dtos.request.product;

import com.example.DATN.constant.Is_Available;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProductColorRequest {
    String colorName;
    List<ProductVariantRequest> variantRequests;
    List<MultipartFile> files;
    List<String> altText;
    Is_Available IsAvailable;
}
