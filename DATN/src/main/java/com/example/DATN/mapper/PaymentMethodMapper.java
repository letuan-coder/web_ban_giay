package com.example.DATN.mapper;

import com.example.DATN.dtos.request.PaymentMethodRequest;
import com.example.DATN.dtos.respone.PaymentMethodResponse;
import com.example.DATN.models.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {
    @Mapping(target = "isAvailable",source = "isAvailable")
    PaymentMethod toPaymentMethod(PaymentMethodRequest request);
    PaymentMethodResponse toPaymentMethodResponse(PaymentMethod paymentMethod);
    void updatePaymentMethod(@MappingTarget PaymentMethod paymentMethod, PaymentMethodRequest request);
}
