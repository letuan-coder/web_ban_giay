package com.example.DATN.dtos.request.banner;

import com.example.DATN.constant.BannerType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
public class UpdateBannerRequest {

    private String bannerName;

    private String redirectUrl;

    private Integer sortOrder;

    private Boolean active = true;

    private LocalDate startAt;

    private LocalDate endAt;

    @NotNull(message = "Banner type is required")
    private BannerType type;
    private MultipartFile file;
}
