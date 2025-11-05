package com.example.DATN.dtos.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SizeRequest {
    @Min(value =20,message = "SIZE_NOT_ALLOW")
    private Integer name;
}