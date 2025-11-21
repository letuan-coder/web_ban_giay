package com.example.DATN.dtos.respone.vnpay;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VnPayResponse {
    private String vnp_ResponseCode;
    private String vnp_Command;
    private String vnp_RequestId;
    private String vnp_TmnCode;
    private String vnp_TxnRef;
    private String vnp_Amount;
    private String vnp_TransactionType;
    private String vnp_OrderInfo;
    private String vnp_ResponseId;
    private String vnp_Message;
    private String vnp_SecureHash;
}
