package com.example.DATN.controllers;

import com.example.DATN.config.VnPayConfig;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.request.vnpay.VnQueryRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.repositories.*;
import com.example.DATN.services.OrderService;
import com.example.DATN.services.VnPayServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@Slf4j
public class VnPayController {
    @Autowired
    private VnPayServices vnpayServices;
    @Autowired
    private OrderMapper orderMapper;

    private final RestTemplate restTemplate;

    @Autowired
    private VnpayRepository vnpayRepository;
    private final String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private UserAddressRepository userAddressRepository;

    public VnPayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/create-payment/{orderCode}")
    public ResponseEntity<?> createPayment(
            @PathVariable String orderCode,
            @RequestBody VnPaymentRequest request,
            HttpServletRequest req) {
        request.setOrderCode(orderCode);
        String response = vnpayServices.createPaymentVNPAY(request, req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/query-payment")
    public ResponseEntity<?> queryPayment(
            @RequestBody VnQueryRequest queryRequest, HttpServletRequest req) {
        return vnpayServices.QueryVnpay(queryRequest.getOrderCode(), req);
    }

//    @PostMapping("/refund")
//    public ResponseEntity<?> refundPayment(
//            @RequestBody VnPayRefundRequest refundRequest, HttpServletRequest req) {
//        return vnpayServices.processRefund(refundRequest, req,null);
//    }

    @GetMapping("/return")
    public ApiResponse<?> vnpayReturn(
            Model model,
            HttpServletRequest request)
            throws UnsupportedEncodingException, JsonProcessingException {

        boolean valid = vnpayServices.verifyReturn(request);
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                fields.put(fieldName, fieldValue);
            }
        }
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");

        // 3. Loại bỏ những trường không dùng để hash
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // 4. Tạo chữ ký từ fields
        String signValue = VnPayConfig.hashAllFields(fields);

        // 6. Lấy thông tin đơn hàng
        String orderCode = request.getParameter("vnp_TxnRef");
        boolean success = valid && "00".equals(request.getParameter("vnp_ResponseCode"));
        String message;
        if (!valid) {
            message = "Chữ ký không hợp lệ";
        } else if (success) {
            message = "Giao dịch thành công";
            vnpayServices.PendingToOrder(model, request);
        } else {
            message = "Giao dịch không thành công";
        }
        return ApiResponse.builder()
                .data(null)
                .message(message)
                .build();
    }

    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(HttpServletRequest request) {

        // Lấy tất cả tham số từ VNPAY
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        log.info("VNPAY IPN HIT: {}", params);

        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_SecureHash = params.get("vnp_SecureHash");

        Map<String, String> response = new HashMap<>();

        try {
            // Verify checksum
            boolean valid = vnpayServices.verifyReturn(request);
            if (!valid) {
                response.put("RspCode", "97"); // Sai checksum → retry
                response.put("Message", "Invalid checksum");
                return ResponseEntity.ok(response);
            }

            // Xử lý thanh toán
            if ("00".equals(vnp_ResponseCode)) {

                response.put("RspCode", "00"); // Thanh toán thành công
                response.put("Message", "Transaction completed successfully");
            } else {
                // Thanh toán thất bại
                response.put("RspCode", "02"); // Đã xử lý thất bại
                response.put("Message", "Transaction failed");
            }

        } catch (Exception e) {
            response.put("RspCode", "01");
            response.put("Message", "Processing error");
        }

        return ResponseEntity.ok(response);
    }
}

