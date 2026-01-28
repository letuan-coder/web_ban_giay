package com.example.DATN.dtos.request;

import com.example.DATN.constant.Is_Available;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentMethodRequest {
    @NotBlank(message = "PAYMENT_METHOD_NAME_NOT_BLANK")
    @Size(max = 255, message = "PAYMENT_METHOD_NAME_MAX_LENGTH_255")
    String displayName;

    @Size(max = 1000, message = "PAYMENT_METHOD_DESCRIPTION_MAX_LENGTH_1000")
    String description;

    @Enumerated(EnumType.STRING)
    Is_Available isAvailable;
}
