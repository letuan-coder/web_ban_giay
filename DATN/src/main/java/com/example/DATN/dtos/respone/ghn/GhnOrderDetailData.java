package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class GhnOrderDetailData {

    @JsonProperty("order_code")
    private String orderCode;

    private String status;

    @JsonProperty("updated_date")
    private OffsetDateTime updatedDate;

    private OffsetDateTime leadtime;

    private List<GhnStatusLogDto> log;
}