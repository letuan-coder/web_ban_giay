package com.example.DATN.dtos.respone;

import com.example.DATN.constant.OrderReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReturnResponse {
    private int id;
    private int orderId;
    private String userName;
    private String reason;
    private OrderReturnStatus status;
    private LocalDateTime createdDate;
    private List<ReturnItemResponse> returnItems;
}
