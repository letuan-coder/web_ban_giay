package com.example.DATN.dtos.respone;

import com.example.DATN.constant.OrderReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReturnResponse {
    private UUID id;
    private UUID orderId;
    private String userName;
    private String reasonReturn;
    private OrderReturnStatus status;
    private LocalDateTime createdDate;
    private List<ReturnItemResponse> returnItems;
}
