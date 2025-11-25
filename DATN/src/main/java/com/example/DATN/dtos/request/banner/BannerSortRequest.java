package com.example.DATN.dtos.request.banner;

import com.example.DATN.constant.BannerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerSortRequest {
    private UUID id;
    private BannerType type;
    private Integer sortOrder;
}