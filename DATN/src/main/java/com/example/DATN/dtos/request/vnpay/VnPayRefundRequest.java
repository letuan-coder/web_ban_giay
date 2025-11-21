package com.example.DATN.dtos.request.vnpay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VnPayRefundRequest {
    private String txnRef;         // vnp_TxnRef
    private Long amount;           // vnp_Amount (số tiền muốn hoàn, theo đơn vị nhỏ nhất VND * 100 nếu cần)
    private String transactionDate; // vnp_TransactionDate: ngày tạo giao dịch gốc (format yyyyMMddHHmmss)
    private String orderInfo;      // vnp_OrderInfo
    private String transactionType;// vnp_TransactionType: loại giao dịch hoàn (VD: "02" cho hoàn tiền)
    private String CreateBy;        // vnp_CreateBy: người tạo yêu cầu hoàn
}
