package com.example.DATN.dtos.request.vnpay;

import lombok.Data;

@Data
public class VNPayQueryRequest {
    private String orderId;     // Mã đơn hàng (vnp_TxnRef)
    private String transDate;   // Ngày giao dịch (yyyyMMddHHmmss)
    private String ipAddr;      // Địa chỉ IP của người dùng
}

