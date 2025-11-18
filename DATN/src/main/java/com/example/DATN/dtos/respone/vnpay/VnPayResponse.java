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
    private String RspCode;
    private String Message;
}
