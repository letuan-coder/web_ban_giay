package com.example.DATN.services;

import com.example.DATN.config.VnPayConfig;
import com.example.DATN.constant.PaymentStatus;
import com.example.DATN.constant.RefundStatus;
import com.example.DATN.constant.StockReservationStatus;
import com.example.DATN.dtos.request.RefundRequest;
import com.example.DATN.dtos.request.vnpay.VnPayRefundRequest;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.respone.order.CancelOrderResponse;
import com.example.DATN.dtos.respone.vnpay.VnPayResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.models.Order;
import com.example.DATN.models.Refund;
import com.example.DATN.models.StockReservation;
import com.example.DATN.models.Vnpay;
import com.example.DATN.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class VnPayServices {
    private final StockReservationRepository stockReservationRepository;
    private final StockRepository stockRepository;
    private final RefundRepository refundRepository;
    private final VnpayRepository vnpayRepository;
    private final RestTemplate restTemplate;
    private final String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
    private final String vnp_HashSecret = VnPayConfig.secretKey;
    private final String version = "2.1.0";
    private final String commandRefund = "refund";
    private final String commmandPay = "pay";
    private final String orderType = "other";
    private final OrderRepository orderRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final RefundService refundService;


    public Boolean CheckPayMent(String orderCode, HttpServletRequest req) {
        try {

            Vnpay vnpay = vnpayRepository.findByVnpTxnRef(orderCode)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_VNPAY_FAIL));
            String vnp_RequestId = VnPayConfig.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";
            String vnp_TxnRef = vnpay.getVnpTxnRef();
            String vnp_OrderInfo = "Kiem tra ket qua GD orderCode:" + vnp_TxnRef;
            String vnp_TransDate = vnpay.getVnp_PayDate();

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());

            String vnp_IpAddr = VnPayConfig.getIpAddress(req);

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_RequestId", vnp_RequestId);
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_TransactionDate", vnp_TransDate);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            String hash_Data = String.join("|",
                    vnp_RequestId, vnp_Version, vnp_Command,
                    vnp_TmnCode, vnp_TxnRef, vnp_TransDate,
                    vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
            String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hash_Data);
            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(vnp_Params, headers);
            ResponseEntity<VnPayResponse> response =
                    restTemplate.postForEntity(VnPayConfig.vnp_ApiUrl, entity, VnPayResponse.class);
            if ("00".equals(response.getBody().getVnp_ResponseCode())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

            return false;
        }
    }

    public ResponseEntity<?> QueryVnpay(String orderCode, HttpServletRequest req) {
        try {
            Vnpay vnpay = vnpayRepository.findByVnpTxnRef(orderCode)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_VNPAY_FAIL));
            String vnp_RequestId = VnPayConfig.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";
            String vnp_TxnRef = vnpay.getVnpTxnRef();
            String vnp_OrderInfo = "Kiem tra ket qua GD orderCode:" + vnp_TxnRef;
            String vnp_TransDate = vnpay.getVnp_PayDate();

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());

            String vnp_IpAddr = VnPayConfig.getIpAddress(req);

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_RequestId", vnp_RequestId);
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_TransactionDate", vnp_TransDate);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            String hash_Data = String.join("|",
                    vnp_RequestId, vnp_Version, vnp_Command,
                    vnp_TmnCode, vnp_TxnRef, vnp_TransDate,
                    vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
            String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hash_Data);
            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(vnp_Params, headers);
            ResponseEntity<VnPayResponse> response =
                    restTemplate.postForEntity(VnPayConfig.vnp_ApiUrl, entity, VnPayResponse.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            throw new ApplicationException(ErrorCode.PAYMENT_VNPAY_FAIL);
        }
    }

    @Transactional
    public void PendingToOrder(Model model,
                               HttpServletRequest request) throws
            UnsupportedEncodingException, JsonProcessingException {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap()
                .forEach((k, v) -> params.put(k, v[0]));
        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderCode = request.getParameter("vnp_TxnRef");
        Boolean isPay = vnpayRepository.existsByVnpTxnRef(orderCode);
        if (isPay) {
            return;
        }
        if (!"00".equals(responseCode)) {
            handleVnPayFailed(orderCode);
            return;
        }
        Vnpay vnpay = Vnpay.builder()
                .vnpTxnRef(orderCode)
                .vnp_BankCode(request.getParameter("vnp_BankCode"))
                .vnp_OrderInfo(request.getParameter("vnp_OrderInfo"))
                .vnp_PayDate(request.getParameter("vnp_PayDate"))
                .vnp_TransactionNo(request.getParameter("vnp_TransactionNo"))
                .vnp_Amount(request.getParameter("vnp_Amount"))
                .vnp_ResponseCode(responseCode)
                .vnp_CardType(request.getParameter("vnp_CardType"))
                .build();
        vnpayRepository.save(vnpay);
        handleVnPaySuccess(orderCode);
    }

    @Transactional
    public void handleVnPayFailed(String orderCode) {

        stockReservationRepository
                .findForUpdateByOrderCodeAndStatus(
                        orderCode, StockReservationStatus.HOLD)
                .ifPresent(r -> {
                    stockRepository.unlockStock(r.getStockId(), r.getQty());
                    r.setStatus(StockReservationStatus.RELEASE);
                });
    }

    @Transactional
    public void handleVnPaySuccess(String orderCode) {

        StockReservation reservations =
                stockReservationRepository.findByOrderCodeAndStatus(
                                orderCode, StockReservationStatus.HOLD)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
        if(reservations.getStatus()==StockReservationStatus.HOLD) {
            int updated = stockRepository.commitStock(
                    reservations.getStockId(),
                    reservations.getQty()
            );
            if (updated == 0) {
                Order order = orderRepository.findByOrderCode(orderCode)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
                RefundRequest refundRequest = RefundRequest.builder()
                        .amount(order.getTotal_price())
                        .reason("PAYMENT SUCCESS BUT OUT OF STOCK " + orderCode)
                        .order(order)
                        .status(RefundStatus.PENDING)
                        .expectedRefundDate(LocalDateTime.now().plusDays(7))
                        .build();
                refundService.createRefundRequest(refundRequest);
                reservations.setStatus(StockReservationStatus.RELEASE);
            } else {
                int mark = stockReservationRepository.markCommitted(orderCode);
                reservations.setStatus(StockReservationStatus.COMMITED);
            }
        }
    }

    public CancelOrderResponse processRefund(
            VnPayRefundRequest refundRequest,
            HttpServletRequest req, Order order, String reason) {
        try {
            Vnpay originalPayment = vnpayRepository.findByVnpTxnRef(refundRequest.getTxnRef())
                    .orElseThrow(() -> new RuntimeException("Original transaction not found for TxnRef: " + refundRequest.getTxnRef()));
            String vnp_RequestId = VnPayConfig.getRandomNumber(8);
            String vnp_Version = version;
            String vnp_Command = commandRefund;
            String vnp_TxnRef = refundRequest.getTxnRef();
            Long amount = refundRequest.getAmount();
            String vnp_Amount = String.valueOf(amount);
            String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;
            String vnp_TransactionNo = originalPayment.getVnp_TransactionNo();
            String vnp_TransactionDate = originalPayment.getVnp_PayDate();
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
            Refund refund = Refund.builder()
                    .order(order)
                    .amount(order.getTotal_price())
                    .reason(reason)
                    .status(RefundStatus.PENDING)
                    .refundTransactionId(vnp_TransactionNo)
                    .expectedRefundDate(LocalDateTime.now().plusDays(5))
                    .build();
            ResponseEntity<VnPayResponse> response = restTemplate
                    .postForEntity(VnPayConfig.vnp_ApiUrl, entity, VnPayResponse.class);
            if ("00".equals(response.getBody().getVnp_ResponseCode())) {
                if ("02".equals(response.getBody().getVnp_ResponseCode())) {
                    vnpayRepository.delete(originalPayment);
                    refund.setStatus(RefundStatus.SUCCESS);
                    refundRepository.save(refund);
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
            CancelOrderResponse refundResponse = CancelOrderResponse.builder()
                    .refundId(refund.getId())
                    .refundStatus(refund.getStatus())
                    .refundAmount(refund.getAmount())
                    .expectedRefundDate(refund.getExpectedRefundDate())
                    .vnpResponseCode(response != null ? response.getBody().getVnp_ResponseCode() : "99")
                    .vnpMessage(response != null ? response.getBody().getVnp_Message() : "VNPAY NO RESPONSE")
                    .vnpTxnRef(refundRequest.getTxnRef())
                    .build();
            return refundResponse;

        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.PAYMENT_VNPAY_FAIL);
        }
    }

    public boolean verifyReturn(HttpServletRequest request)
            throws UnsupportedEncodingException {

        Map<String, String> fields = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (!k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")) {
                fields.put(k, v[0]);
            }
        });

        String secureHash = request.getParameter("vnp_SecureHash");
        String signValue = VnPayConfig.hashAllFields(fields);

        return signValue.equals(secureHash);
    }

    public Map<String, String> handleIpn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        try {

            Map<String, String> fields = new HashMap<>();
            Enumeration<String> params = request.getParameterNames();

            while (params.hasMoreElements()) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnpSecureHash = fields.get("vnp_SecureHash");
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            String signValue = VnPayConfig.hashAllFields(fields);

            if (!signValue.equals(vnpSecureHash)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
                return response;
            }

            String orderCode = request.getParameter("vnp_TxnRef");
            long vnpAmount = Long.parseLong(request.getParameter("vnp_Amount")) / 100;
            String responseCode = request.getParameter("vnp_ResponseCode");

            Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
            if (order == null) {
                response.put("RspCode", "01");
                response.put("Message", "Order not Found");
                return response;
            }

            // ===== 5. Check amount =====
            if (order.getTotal_price().longValue() != vnpAmount) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid Amount");
                return response;
            }

            // ===== 6. Check trạng thái cũ =====
            if (order.getPaymentStatus() != PaymentStatus.PENDING) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }

            // ===== 7. Update DB =====
            if ("00".equals(responseCode)) {
                order.setPaymentStatus(PaymentStatus.SUCCESS);
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
            }

            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            // ===== 8. Trả kết quả =====
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;

        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknow error");
            return response;
        }
    }

    public String createPaymentVNPAY
            (VnPaymentRequest request, HttpServletRequest req) {

        try {
            String vnp_Version = version;
            String vnp_Command = commmandPay;
            request.setAmount(request.getAmount().longValue());
            req.setAttribute("order", request.getOrderCode());

            long amount = request.getAmount() * 100L;
            String bankCode = request.getBankCode();

            String vnp_TxnRef = request.getOrderCode();
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
            return paymentUrl;


        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.PAYMENT_VNPAY_FAIL);
        }
    }
}
