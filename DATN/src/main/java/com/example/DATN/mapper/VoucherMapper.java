package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.voucher.VoucherResponse;
import com.example.DATN.models.Voucher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoucherMapper {

    VoucherResponse toResponse (Voucher voucher);
}
