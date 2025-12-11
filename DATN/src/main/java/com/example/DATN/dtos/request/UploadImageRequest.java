package com.example.DATN.dtos.request;

import com.example.DATN.models.Banner;
import com.example.DATN.models.ImageProduct;
import com.example.DATN.models.Product;
import com.example.DATN.models.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UploadImageRequest {
    private Banner banner;
    private Product product;
    private ImageProduct imageProduct;
    private User userAvatar;
    private MultipartFile file;
    private String imageUrl;
    private String altText;
}
