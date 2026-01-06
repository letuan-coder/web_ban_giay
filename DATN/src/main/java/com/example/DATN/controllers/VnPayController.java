package com.example.DATN.controllers;

import com.example.DATN.config.VnPayConfig;
import com.example.DATN.constant.OrderStatus;
import com.example.DATN.constant.PaymentMethodEnum;
import com.example.DATN.constant.PaymentStatus;
import com.example.DATN.dtos.request.vnpay.VnPayRefundRequest;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.request.vnpay.VnQueryRequest;
import com.example.DATN.dtos.respone.order.PendingOrderItem;
import com.example.DATN.dtos.respone.order.PendingOrderRedis;
import com.example.DATN.dtos.respone.vnpay.VnPayResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.models.*;
import com.example.DATN.models.Embeddable.ShippingAddress;
import com.example.DATN.repositories.*;
import com.example.DATN.services.OrderService;
import com.example.DATN.services.VnPayServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/api/vnpay")
public class VnPayController {

    @Autowired
    private VnPayServices vnpayServices;
    @Autowired
    private OrderMapper orderMapper;

    private final RestTemplate restTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
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

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorCode.PAYMENT_METHOD_NOT_EXISTED);
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(
            @RequestBody VnPayRefundRequest refundRequest, HttpServletRequest req) {
        return vnpayServices.processRefund(refundRequest, req);
    }

    @GetMapping("/return")
    public String vnpayReturn(
            Model model,
            HttpServletRequest request) throws UnsupportedEncodingException, JsonProcessingException {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap()
                .forEach((k, v) -> params.put(k, v[0]));

        // verify checksum
        boolean valid = vnpayServices.verifyReturn(request);

        String orderCode = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        model.addAttribute("params", params);
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("success", valid && "00".equals(responseCode));
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
        String key = "ORDER_PENDING:" + orderCode;
        String json = (String) redisTemplate.opsForValue().get(key);

        if (json == null) {
            throw new ApplicationException(ErrorCode.ORDER_NOT_FOUND);
        } else {
            PendingOrderRedis pending =
                    objectMapper.readValue(json, PendingOrderRedis.class);
            Order order = new Order();
            order = orderMapper.toOrder(pending);
            ShippingAddress address = orderMapper.toShipping(pending.getUserAddresses());
            List<PendingOrderItem> pendingOrderItems = pending.getItems();
            User user = userRepository.findById(pending.getUserId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
            order.setUser(user);
            order.setUserAddresses(address);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentMethod(PaymentMethodEnum.VNPAY);
            order.setCreatedAt(LocalDateTime.now());
            List<OrderItem> items = new ArrayList<>();
            for (PendingOrderItem item : pendingOrderItems) {
                OrderItem orderItem = new OrderItem();
                ProductVariant productVariant = productVariantRepository.findBysku(item.getSku())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
                Integer quantity = item.getQuantity();
                orderItem.setQuantity(quantity);
                orderItem.setOrder(order);
                orderItem.setProductVariant(productVariant);
                orderItem.setPrice(item.getPrice());
                orderItem.setWeight(item.getWeight());
                orderItem.setHeight(item.getHeight());
                orderItem.setWidth(item.getWidth());
                orderItem.setLength(item.getLength());
                orderItem.setRated(false);
                items.add(orderItem);
            }
            order.setItems(items);
            orderRepository.save(order);
            redisTemplate.delete(key);
            return "vnpay_return";

        }
    }

    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(HttpServletRequest request) {

        // Lấy tất cả tham số từ VNPAY
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

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
                // Thanh toán thành công → update DB và Redis

                response.put("RspCode", "00"); // Thanh toán thành công
                response.put("Message", "Transaction completed successfully");
            } else {
                // Thanh toán thất bại
                response.put("RspCode", "02"); // Đã xử lý thất bại
                response.put("Message", "Transaction failed");
            }

        } catch (Exception e) {
            // Lỗi trong quá trình xử lý → VNPAY retry
            response.put("RspCode", "01");
            response.put("Message", "Processing error");
        }

        return ResponseEntity.ok(response);
    }
}

