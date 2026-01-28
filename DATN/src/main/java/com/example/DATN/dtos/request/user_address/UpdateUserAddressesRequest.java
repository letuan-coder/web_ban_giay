package com.example.DATN.dtos.request.user_address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserAddressesRequest {
    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phoneNumber;

    @NotBlank(message = "Province ID is required")
    private String provinceId;
    private String provinceName;

    @NotBlank(message = "District ID is required")
    private String districtId;
    private String districtName;

    @NotBlank(message = "Ward ID is required")
    private String wardId;
    private String wardName;

    @NotBlank(message = "Street detail is required")
    private String streetDetail;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
