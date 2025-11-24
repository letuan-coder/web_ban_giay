package com.example.DATN.controllers;

import com.example.DATN.dtos.request.supplier.SupplierRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.supplier.SupplierResponse;
import com.example.DATN.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ApiResponse<SupplierResponse> createSupplier(
            @RequestBody @Valid SupplierRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .data(supplierService.createSupplier(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<SupplierResponse>> getAllSuppliers() {
        return ApiResponse.<List<SupplierResponse>>builder()
                .data(supplierService.getAllSuppliers())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ApiResponse.<SupplierResponse>builder()
                .data(supplierService.getSupplierById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SupplierResponse> updateSupplier(@PathVariable Long id, @RequestBody @Valid SupplierRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .data(supplierService.updateSupplier(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ApiResponse.<Void>builder()
                .message("Supplier deleted successfully")
                .build();
    }
}
