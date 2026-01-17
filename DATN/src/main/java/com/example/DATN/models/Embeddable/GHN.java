package com.example.DATN.models.Embeddable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GHN {

    // Mã đơn hàng GHN
    private String ghnOrderCode;

    // Mã phân loại nội bộ của GHN
    private String sortCode;


    // Loại vận chuyển (truck, motor…)
    private String transType;

    // Mã phường/xã
    private String wardEncode;

    // Mã quận/huyện
    private String districtEncode;

    // Tổng phí GHN (total_fee)
    private Integer totalFee;

    // Phí dịch vụ chính (main_service)
    private Integer mainServiceFee;

    // Thời gian dự kiến giao hàng
    private LocalDateTime expectedDeliveryTime;

    // Đối tác vận hành (operation_partner)
    private String operationPartner;

    // Thời gian cập nhật trạng thái lần cuối
    private LocalDateTime ghnLastUpdated;
}
