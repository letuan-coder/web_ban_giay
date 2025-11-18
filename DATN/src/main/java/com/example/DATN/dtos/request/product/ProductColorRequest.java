package com.example.DATN.dtos.request.product;

import com.example.DATN.dtos.request.ColorRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductColorRequest {
    UUID productId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String colorName;
    ColorRequest color;
    List<ProductVariantRequest> variantRequests;
    List<MultipartFile> files;
    List<String> altText;
}
