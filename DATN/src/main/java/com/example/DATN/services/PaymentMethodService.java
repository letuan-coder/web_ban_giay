package com.example.DATN.services;

import com.example.DATN.dtos.request.PaymentMethodRequest;
import com.example.DATN.dtos.respone.PaymentMethodResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.PaymentMethodMapper;
import com.example.DATN.models.PaymentMethod;
import com.example.DATN.repositories.PaymentMethodRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentMethodService {
    PaymentMethodRepository paymentMethodRepository;
    PaymentMethodMapper paymentMethodMapper;

    public PaymentMethodResponse createPaymentMethod(PaymentMethodRequest request) {
        if (paymentMethodRepository.existsBydisplayName(request.getDisplayName())) {
            throw new ApplicationException(ErrorCode.PAYMENT_METHOD_EXISTED);
        }

        PaymentMethod paymentMethod = paymentMethodMapper.toPaymentMethod(request);
        return paymentMethodMapper.toPaymentMethodResponse(paymentMethodRepository.save(paymentMethod));
    }

    public List<PaymentMethodResponse> getAllPaymentMethods() {
        return paymentMethodRepository.findAll().stream()
                .map(paymentMethodMapper::toPaymentMethodResponse)
                .collect(Collectors.toList());
    }

    public PaymentMethodResponse getPaymentMethodById(Long id) {
        return paymentMethodRepository.findById(id)
                .map(paymentMethodMapper::toPaymentMethodResponse)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_EXISTED));
    }

    public PaymentMethodResponse updatePaymentMethod(Long id, PaymentMethodRequest request) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_EXISTED));
        paymentMethod.setIsAvailable(request.getIsAvailable());

        paymentMethodMapper.updatePaymentMethod(paymentMethod, request);
        return paymentMethodMapper.toPaymentMethodResponse(paymentMethodRepository.save(paymentMethod));
    }

}
