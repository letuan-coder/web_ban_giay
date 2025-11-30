package com.example.DATN.services;

import com.example.DATN.config.VnPayConfig;
import com.example.DATN.dtos.request.vnpay.VnPayRefundRequest;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.respone.vnpay.VnPayResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.models.Order;
import com.example.DATN.models.Vnpay;
import com.example.DATN.repositories.VnpayRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@AllArgsConstructor
public class VnPayServices {
    private final VnpayRepository vnpayRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final RestTemplate restTemplate;
    private final OrderService orderService;
    private final String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
    private final String vnp_HashSecret = VnPayConfig.secretKey;
    private final String version = "2.1.0";
    private final String commandRefund= "refund";
    private final String commmandPay= "pay";
    private final String orderType = "other";

    public ResponseEntity<?> processRefund(
            VnPayRefundRequest refundRequest,
            HttpServletRequest req) {
        try {
            Vnpay originalPayment = vnpayRepository.findByVnpTxnRef(refundRequest.getTxnRef())
                    .orElseThrow(() -> new RuntimeException("Original transaction not found for TxnRef: " + refundRequest.getTxnRef()));
            String vnp_RequestId = VnPayConfig.getRandomNumber(8);
            String vnp_Version = version;
            String vnp_Command = commandRefund;
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
            if (response.getBody().getVnp_ResponseCode() == "00") {
                if (refundRequest.getTransactionType() == "02") {
                    vnpayRepository.delete(originalPayment);
                } else {
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

    public ResponseEntity<?> createPaymentVNPAY
            (VnPaymentRequest request, HttpServletRequest req)
    {
        try {

            Order order = orderService.findOrderById(request.getOrderId());
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorCode.ORDER_NOT_FOUND);
            }
            String vnp_Version = version;
            String vnp_Command= commmandPay;
            request.setAmount(order.getTotal_price().longValue());
            req.setAttribute("order", order.getId());

            long amount = request.getAmount() * 100L;
            String bankCode = request.getBankCode();

            String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
            String vnp_IpAddr = VnPayConfig.getIpAddress(req);
            String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");

            if (bankCode != null && !bankCode.isEmpty()) {
                vnp_Params.put("vnp_BankCode", bankCode);
                System.out.println(bankCode);
            }
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", request.getLanguage() != null ? request.getLanguage() : "vn");
            vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Sắp xếp key để tạo chuỗi hash
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnp_SecureHash);

            String paymentUrl = VnPayConfig.vnp_PayUrl + "?" + query;

            Map<String, Object> response = new HashMap<>();
            response.put("code", "00");
            response.put("message", "success");
            response.put("data", paymentUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of());
        }
    }
}
