package com.example.DATN.controllers;

import com.example.DATN.dtos.request.PaymentMethodRequest;
import com.example.DATN.dtos.respone.PaymentMethodResponse;
import com.example.DATN.services.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {
    PaymentMethodService paymentMethodService;

    @PostMapping
    public PaymentMethodResponse createPaymentMethod(
            @RequestBody @Valid PaymentMethodRequest request) {
        return paymentMethodService.createPaymentMethod(request);
    }

    @GetMapping
    public List<PaymentMethodResponse> getAllPaymentMethods() {
        return paymentMethodService.getAllPaymentMethods();
    }

    @GetMapping("/{id}")
    public PaymentMethodResponse getPaymentMethodById(
            @PathVariable Long id) {
        return paymentMethodService.getPaymentMethodById(id);
    }

    @PatchMapping("/{id}")
    public PaymentMethodResponse updatePaymentMethod(
            @PathVariable Long id,
            @RequestBody @Valid PaymentMethodRequest request) {
        return paymentMethodService.updatePaymentMethod(id, request);
    }
}
