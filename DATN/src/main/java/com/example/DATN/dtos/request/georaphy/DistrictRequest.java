package com.example.DATN.dtos.request.georaphy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictRequest {
    private String code;
    private String name;
    private String slug;
    private String type;
    private String nameWithType;
    private String path;
    private String pathWithType;
    private String parentCode;
    private String provinceId;
}
