package com.example.DATN.dtos.respone.supplier;

import com.example.DATN.constant.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierResponse {
    private UUID id;
    private String name;
    private String taxCode;
    private String email;
    private String phoneNumber;
    private String supplierAddress;
    private SupplierStatus status;
}
