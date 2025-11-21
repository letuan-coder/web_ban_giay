package com.example.DATN.controllers;

import com.example.DATN.config.VnPayConfig;
import com.example.DATN.dtos.request.vnpay.VnPayRefundRequest;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.request.vnpay.VnQueryRequest;
import com.example.DATN.dtos.respone.vnpay.VnPayResponse;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.Order;
import com.example.DATN.models.Vnpay;
import com.example.DATN.repositories.VnpayRepository;
import com.example.DATN.services.OrderService;
import com.example.DATN.services.VnPayServices;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller

@RequestMapping("/api/vnpay")
public class VnPayController {

    @Autowired
    private  VnPayServices vnpayServices;
    private final RestTemplate restTemplate;
    @Autowired
    private OrderService orderService;
    @Autowired
    private VnpayRepository vnpayRepository;
    private final String vnp_Version = "2.1.0";
    private final String vnp_Command = "pay";
    private final String orderType = "other";
    private final String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

    public VnPayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/create-payment/{orderId}")
    public ResponseEntity<?> createPayment(
            @PathVariable Long orderId,
            @RequestBody VnPaymentRequest request,
            HttpServletRequest req) {
        try {
            Order order=orderService.findOrderById(orderId);
            if(order == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorCode.ORDER_NOT_FOUND);
            }
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
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/query-payment")
    public ResponseEntity<?> queryPayment(
            @RequestBody VnQueryRequest queryRequest, HttpServletRequest req) {
        try {
            String vnp_RequestId = VnPayConfig.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";
            String vnp_TxnRef = queryRequest.getOrderId();
            String vnp_OrderInfo = "Kiem tra ket qua GD OrderId:" + vnp_TxnRef;
            String vnp_TransDate = queryRequest.getTransDate();

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
            String hash_Data = String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode, vnp_TxnRef, vnp_TransDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
            String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hash_Data);
            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(vnp_Params, headers);
            ResponseEntity<VnPayResponse> response = restTemplate.postForEntity(VnPayConfig.vnp_ApiUrl, entity, VnPayResponse.class);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorCode.PAYMENT_METHOD_NOT_EXISTED);
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(
            @RequestBody VnPayRefundRequest refundRequest, HttpServletRequest req)
    {
        return vnpayServices.processRefund(refundRequest,req);
    }

    @GetMapping("/return")
    public String vnpayReturn(
            Model model,
            HttpServletRequest request) {
        // Lấy tham số từ VNPAY trả về
        Map<String, String[]> parameterMap = request.getParameterMap();
        // Đưa tất cả params sang giao diện Thymeleaf
        Map<String, String> param = new HashMap<>();
        parameterMap.forEach((k, v) -> param.put(k, v[0]));
        model.addAttribute("params", param);
        try {
        /*  IPN URL: Record payment results from VNPAY
        Implementation steps:
        Check checksum
        Find transactions (vnp_TxnRef) in the database (checkOrderId)
        Check the payment status of transactions before updating (checkOrderStatus)
        Check the amount (vnp_Amount) of transactions before updating (checkAmount)
        Update results to Database
        Return recorded results to VNPAY
        */

            // ex:  	PaymnentStatus = 0; pending
            //              PaymnentStatus = 1; success
            //              PaymnentStatus = 2; Faile

            //Begin process return from VNPAY
            Map fields = new HashMap();
            for (Enumeration params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }


            String signValue = VnPayConfig.hashAllFields(fields);
            if (signValue.equals(vnp_SecureHash)) {

                boolean checkOrderId = true; // vnp_TxnRef exists in your database
                boolean checkAmount = true;
                boolean checkOrderStatus = true; // PaymnentStatus = 0 (pending)
                if (checkOrderId) {
                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                                BigDecimal vnpAmount = new BigDecimal(request.getParameter("vnp_Amount")).divide(new BigDecimal(100));
                                Vnpay savedPayment =   Vnpay.builder()
                                        .vnp_PayDate(request.getParameter("vnp_PayDate"))
                                        .vnp_TransactionNo(request.getParameter("vnp_TransactionNo"))
                                        .vnp_BankCode(request.getParameter("vnp_BankCode"))
                                        .vnp_Amount(vnpAmount.toString())
                                        .vnp_OrderInfo(request.getParameter("vnp_OrderInfo"))
                                        .vnp_ResponseCode(request.getParameter("vnp_ResponseCode"))
                                        .vnpTxnRef(request.getParameter("vnp_TxnRef"))
                                        .build();
                                vnpayRepository.save(savedPayment);
                                //Here Code update PaymnentStatus = 1 into your Database
                            } else {
                                // Here Code update PaymnentStatus = 2 into your Database
                            }
                            System.out.print("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
                        } else {
                            System.out.print("{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}");
                        }
                    } else {
                        System.out.print("{\"RspCode\":\"04\",\"Message\":\"Invalid Amount\"}");
                    }
                } else {
                    System.out.print("{\"RspCode\":\"01\",\"Message\":\"Order not Found\"}");
                }
            } else {
                System.out.print("{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}");
            }
        } catch (Exception e) {
            System.out.print("{\"RspCode\":\"99\",\"Message\":\"Unknow error\"}");
        }
        return "vnpay_return";
    }
}

