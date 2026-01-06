package com.example.DATN.dtos.request.warehouse;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateCentralRequest {
    UUID id;
    Boolean isCentral;
}
