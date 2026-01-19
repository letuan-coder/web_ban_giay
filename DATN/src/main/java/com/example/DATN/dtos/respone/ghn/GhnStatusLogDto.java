package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GhnStatusLogDto {

    private String status;
    @JsonProperty("updated_date")
    private LocalDateTime updatedDate;
}