package com.example.DATN.dtos.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreationProductVariantRequest {
    private ProductRequest productRequest;
    private ProductVariantRequest variantRequest;
    private List<MultipartFile> files;
    private List<String> altText;
}
