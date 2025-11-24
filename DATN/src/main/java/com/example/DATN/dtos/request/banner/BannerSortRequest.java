package com.example.DATN.dtos.request.banner;

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
    private Integer sortOrder;
}