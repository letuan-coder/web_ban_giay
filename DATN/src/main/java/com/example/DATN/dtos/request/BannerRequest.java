package com.example.DATN.dtos.request;

import com.example.DATN.constant.BannerType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BannerRequest {
    private UUID id;
    @NotNull(message = "Image URL is required")
    private String imageUrl;

    private String bannerName;

    private String redirectUrl;

    private Integer sortOrder;

    private Boolean active = true;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @NotNull(message = "Banner type is required")
    private BannerType type;
    private MultipartFile file;
}
