package com.example.DATN.models.Embeddable;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAddress {
    String receiverName;
    String phoneNumber;
    String provinceName;
    String districtName;
    Integer district_Id;
    String wardCode;
    String wardName;
    String streetDetail;
    String fullDetail;
}
