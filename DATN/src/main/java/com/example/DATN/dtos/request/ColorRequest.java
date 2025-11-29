package com.example.DATN.dtos.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColorRequest {
    private String name;
    //@HexCodeConstraint(message = "INVALID_HEX_CODE")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hexCode;
}