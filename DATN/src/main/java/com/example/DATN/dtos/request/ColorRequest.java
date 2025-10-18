package com.example.DATN.dtos.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColorRequest {
    private String name;
    //@HexCodeConstraint(message = "INVALID_HEX_CODE")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hexCode;
}