package com.example.DATN.services;

import com.example.DATN.config.VnPayConfig;
import com.example.DATN.dtos.request.vnpay.VnPayRefundRequest;
import com.example.DATN.dtos.respone.vnpay.VnPayResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.models.Vnpay;
import com.example.DATN.repositories.VnpayRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Service
@AllArgsConstructor
public class VnPayServices {
    private final VnpayRepository vnpayRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final RestTemplate restTemplate;
    private final String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
    private final String vnp_HashSecret = VnPayConfig.secretKey;
    public ResponseEntity<?> processRefund(
            VnPayRefundRequest refundRequest,
            HttpServletRequest req) {
        try {
            Vnpay originalPayment = vnpayRepository.findByVnpTxnRef(refundRequest.getTxnRef())
                    .orElseThrow(() -> new RuntimeException("Original transaction not found for TxnRef: " + refundRequest.getTxnRef()));
            String vnp_RequestId = VnPayConfig.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "refund";
            String vnp_TxnRef = refundRequest.getTxnRef();
            Long amount = refundRequest.getAmount() * 100L;
            String vnp_Amount = String.valueOf(amount);
            String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;
            String vnp_TransactionNo = originalPayment.getVnp_TransactionNo();
            String vnp_TransactionDate = refundRequest.getTransactionDate();
            String vnp_CreateBy = "admin";

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            String vnp_IpAddr = VnPayConfig.getIpAddress(req);

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_RequestId", vnp_RequestId);
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_TransactionType", refundRequest.getTransactionType());
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_TransactionNo", vnp_TransactionNo);
            vnp_Params.put("vnp_TransactionDate", vnp_TransactionDate);
            vnp_Params.put("vnp_CreateBy", vnp_CreateBy);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            String hash_Data = String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                    refundRequest.getTransactionType(), vnp_TxnRef, vnp_Amount, vnp_TransactionNo, vnp_TransactionDate,
                    vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

            String vnp_SecureHash = VnPayConfig.hmacSHA512(vnp_HashSecret, hash_Data);
            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(vnp_Params, headers);

            ResponseEntity<VnPayResponse> response = restTemplate.postForEntity(VnPayConfig.vnp_ApiUrl, entity, VnPayResponse.class);
            if(response.getBody().getVnp_ResponseCode()=="00") {
                if(refundRequest.getTransactionType()=="02") {
                    vnpayRepository.delete(originalPayment);
                }else {
                    String oldAmountStr = originalPayment.getVnp_Amount();
                    Long oldAmount = Long.parseLong(oldAmountStr);
                    Long refundAmount = refundRequest.getAmount();
                    Long newAmount = oldAmount - refundAmount;
                    if (newAmount < 0) {
                        throw new ApplicationException(ErrorCode.REFUND_NOT_ALLOWDED);
                    }
                    originalPayment.setVnp_Amount(newAmount.toString());
                }
            }
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
