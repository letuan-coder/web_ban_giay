package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColorResponse {
    private String code;
    private String name;
    private String hexCode;
}
