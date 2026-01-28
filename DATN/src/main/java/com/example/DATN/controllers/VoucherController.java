package com.example.DATN.controllers;

import com.example.DATN.dtos.request.voucher.CreateVoucherRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.voucher.VoucherResponse;
import com.example.DATN.models.Voucher;
import com.example.DATN.services.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @PostMapping
    public ApiResponse<VoucherResponse> createVoucher(
            @RequestBody @Valid CreateVoucherRequest request){
        VoucherResponse response = voucherService.createVoucher(request);
        return ApiResponse.<VoucherResponse>builder()
                .data(response)
                .build();
    }


    @GetMapping("/admin/all")
    public ApiResponse<List<Voucher>> getAllVoucherClaim(){
        return ApiResponse.<List<Voucher>>builder()
                .data(voucherService.getAllVoucher())
                .build();
    }
    @PostMapping("/search")
    public ApiResponse<VoucherResponse> findVoucher(
            @RequestBody @Valid String code){
        VoucherResponse response = voucherService.findVoucherByCode(code);
        return ApiResponse.<VoucherResponse>builder()
                .data(response)
                .build();
    }

}
