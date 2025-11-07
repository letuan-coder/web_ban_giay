package com.example.DATN.controllers;

import com.example.DATN.config.VnPayConfig;
import com.example.DATN.constant.Util.VnPayUtil;
import com.example.DATN.dtos.request.vnpay.VNPayQueryRequest;

import com.nimbusds.jose.shaded.gson.JsonObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/api/vnpay")
public class VNPayQueryController {

    private static final String VNP_VERSION = "2.1.0";
    private static final String VNP_COMMAND = "querydr";
    private static final String VNP_TMNCODE = VnPayConfig.vnp_TmnCode;  // Mã TMN của bạn
    private static final String VNP_SECRET_KEY = VnPayConfig.secretKey; // Khóa bí mật do VNPay cấp
    private static final String VNP_API_URL = VnPayConfig.vnp_ApiUrl;

    @PostMapping("/query")
    public ResponseEntity<String> queryTransaction(@RequestBody VNPayQueryRequest req) {
        try {
            String vnp_RequestId = getRandomNumber(8);
            String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            JsonObject vnp_Params = new JsonObject();

            vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
            vnp_Params.addProperty("vnp_Version", VNP_VERSION);
            vnp_Params.addProperty("vnp_Command", VNP_COMMAND);
            vnp_Params.addProperty("vnp_TmnCode", VNP_TMNCODE);
            vnp_Params.addProperty("vnp_TxnRef", req.getOrderId());
            vnp_Params.addProperty("vnp_OrderInfo", "Kiểm tra giao dịch OrderId:" + req.getOrderId());
            vnp_Params.addProperty("vnp_TransactionDate", req.getTransDate());
            vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.addProperty("vnp_IpAddr", req.getIpAddr());

            // Dữ liệu hash
            String hashData = String.join("|",
                    vnp_RequestId, VNP_VERSION, VNP_COMMAND, VNP_TMNCODE,
                    req.getOrderId(), req.getTransDate(), vnp_CreateDate,
                    req.getIpAddr(), "Kiểm tra giao dịch OrderId:" + req.getOrderId());

            String vnp_SecureHash = VnPayUtil.hmacSHA512(VNP_SECRET_KEY, hashData);
            vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(vnp_Params.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(VNP_API_URL, entity, String.class);

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private String getRandomNumber(int len) {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }
}
