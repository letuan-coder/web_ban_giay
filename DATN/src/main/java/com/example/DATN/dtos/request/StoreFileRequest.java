package com.example.DATN.dtos.request;

import com.example.DATN.models.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class StoreFileRequest {
    private ImageProduct imageProduct;
    private Product product;
    private Banner banner;
    private User userAvatar;
    private ImageOrderReturn imageOrderReturn;
    private String fileName;
    private MultipartFile file;
}
