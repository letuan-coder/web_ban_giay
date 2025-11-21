package com.example.DATN.dtos.respone;

import com.example.DATN.constant.BannerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BannerResponse {
    private UUID id;
    private String imageUrl;
    private String redirectUrl;
    private Integer sortOrder;
    private Boolean active;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BannerType type;
}
