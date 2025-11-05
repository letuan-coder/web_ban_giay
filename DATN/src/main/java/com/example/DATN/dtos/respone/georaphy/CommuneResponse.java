package com.example.DATN.dtos.respone.georaphy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommuneResponse {
    @JsonIgnore
    private String id;
    @JsonIgnore
    private String code;
    @JsonIgnore
    private String name;
    @JsonIgnore
    private String slug;
    @JsonIgnore
    private String type;

    private String pathWithType;
    private String nameWithType;

    @JsonIgnore
    private String path;

    @JsonIgnore
    private String parentCode;
}
