package com.example.DATN.services;

import com.example.DATN.constant.OrderStatus;
import com.example.DATN.constant.ShippingStatus;
import com.example.DATN.dtos.request.RegisterStoreGHNRequest;
import com.example.DATN.dtos.request.ghtk.GhnOrderInfo;
import com.example.DATN.dtos.respone.ghn.*;
import com.example.DATN.models.Embeddable.GHN;
import com.example.DATN.models.Order;
import com.example.DATN.models.Store;
import com.example.DATN.models.WareHouse;
import com.example.DATN.repositories.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhnService {
    private static String ghnTokenStatic;
    private static String createUrlStatic;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    @Value("${ghn.api.token}")
    private String ghnToken;

    @Value("${ghn.api.create.url}")
    private String createUrl;

    @PostConstruct
    public void init() {
        ghnTokenStatic = this.ghnToken;
        createUrlStatic = this.createUrl;
    }

    @Value("${ghn.api.calculate-fee.url}")
    private String url;

    @Value("${ghn.api.register.url}")
    private String registerUrl;

    public void createOrder(GhnOrderInfo ghnOrderInfo, Order order) {
        // URL GHN (sandbox)
        String url = createUrlStatic;

        // Tạo RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Tạo header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnTokenStatic);  // thay bằng token thật
        headers.set("ShopId", order.getStoreDelivered().getStoreCodeGHN().toString());   // thay bằng shopId thật

        // Tạo HttpEntity với body và header
        HttpEntity<GhnOrderInfo> requestEntity = new HttpEntity<>(ghnOrderInfo, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Tạo đơn GHN thành công: " + response.getBody());
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                GhnCreateOrderResponse createOrderResponse =
                        mapper.readValue(response.getBody(), GhnCreateOrderResponse.class);
                if (order.getGhn() == null) {
                    order.setGhn(new GHN());
                }
                order.getGhn().setGhnOrderCode(createOrderResponse.getData().getOrderCode());
                order.getGhn().setExpectedDeliveryTime(
                                createOrderResponse.getData()
                                .getExpectedDeliveryTime()
                                .toLocalDateTime());
                order.setOrderStatus(OrderStatus.DELEVERING);
                order.setGhnStatus(ShippingStatus.PICKING);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            } else {
                System.out.println("Lỗi khi tạo đơn GHN: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public GhnOrderSyncResponse getOrderDetail(String orderCode) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);

        Map<String, String> body = Map.of(
                "order_code", orderCode
        );

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<GhnOrderSyncResponse> response =
                restTemplate.exchange(
                       "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/detail",
                        HttpMethod.POST,
                        request,
                        GhnOrderSyncResponse.class
                );

        return response.getBody();
    }

    public ResponseEntity<GhnCalculateFeeResponse> calculateShippingFee(CalculateFeeRequest request,Integer headerKey) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);
        headers.add("Content-Type", "application/json; charset=UTF-8");

        headers.set("ShopId", "198233");

        HttpEntity<CalculateFeeRequest> entity =
                new HttpEntity<>(request, headers);
        log.info(entity.toString());
        ObjectMapper mapper = new ObjectMapper();
        log.info("JSON SENT = {}", mapper.writeValueAsString(request));
        ResponseEntity<GhnCalculateFeeResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        GhnCalculateFeeResponse.class
                );
        log.info("RAW RESPONSE = {}", response.getBody());

        return ResponseEntity.ok( response.getBody());
    }

    public ResponseEntity<GhnRegisterShopResponse> registerShop(Store request) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RegisterStoreGHNRequest body = RegisterStoreGHNRequest.builder()
                .districtId(request.getDistrictCode())
                .wardCode(request.getWardCode())
                .name(request.getName())
                .phone(request.getPhoneNumber())
                .address(request.getLocation())
                .build();

        HttpEntity<RegisterStoreGHNRequest> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<GhnRegisterShopResponse> response = restTemplate.exchange(
                registerUrl,
                HttpMethod.POST,
                entity,
                GhnRegisterShopResponse.class
        );
        GhnRegisterShopResponse bodyRes = response.getBody();

        if (bodyRes == null || bodyRes.getData() == null) {
            throw new RuntimeException("GHN response invalid");
        }

        Integer shopId = bodyRes.getData().getShopId();
        request.setStoreCodeGHN(shopId);
        return ResponseEntity.ok(response.getBody());
    }
    public ResponseEntity<GhnRegisterShopResponse> registerWareHouse(WareHouse request)
            throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RegisterStoreGHNRequest body = RegisterStoreGHNRequest.builder()
                .districtId(request.getDistrictCode())
                .wardCode(request.getWardCode())
                .name(request.getName())
                .phone(request.getPhoneNumber())
                .address(request.getLocation())
                .build();

        HttpEntity<RegisterStoreGHNRequest> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<GhnRegisterShopResponse> response = restTemplate.exchange(
                registerUrl,
                HttpMethod.POST,
                entity,
                GhnRegisterShopResponse.class
        );
        GhnRegisterShopResponse bodyRes = response.getBody();

        if (bodyRes == null || bodyRes.getData() == null) {
            throw new RuntimeException("GHN response invalid");
        }

        Integer shopId = bodyRes.getData().getShopId();
        request.setWarehouseCodeGHN(shopId);
        return ResponseEntity.ok(response.getBody());
    }




}
