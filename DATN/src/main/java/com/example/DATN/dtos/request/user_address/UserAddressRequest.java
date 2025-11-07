package com.example.DATN.dtos.request.user_address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAddressRequest {

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phoneNumber;

    @NotBlank(message = "Province ID is required")
    private String provinceId;

    @NotBlank(message = "District ID is required")
    private String districtId;

    @NotBlank(message = "Commune ID is required")
    private String communeId;

    @NotBlank(message = "Street detail is required")
    private String streetDetail;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
