package com.example.DATN.services;

import com.example.DATN.dtos.request.ghtk.GhnOrderInfo;
import com.example.DATN.dtos.respone.ghn.CalculateFeeRequest;
import com.example.DATN.dtos.respone.ghn.ShippingFeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhnService {
    @Value("${ghn.api.token}")
    static String ghnToken;

    @Value("${ghn.api.create.url}")
    static String create_url;
    @Value("${ghn.api.calculate-fee.url}")
    private String url;
    public static void createOrder(GhnOrderInfo ghnOrderInfo) {
        // URL GHN (sandbox)
        String url =create_url;

        // Tạo RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Tạo header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token",ghnToken );  // thay bằng token thật
        headers.set("ShopId", "YOUR_SHOP_ID");   // thay bằng shopId thật

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
            } else {
                System.out.println("Lỗi khi tạo đơn GHN: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ShippingFeeResponse calculateShippingFee(CalculateFeeRequest request){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);
        headers.set("ShopId", "885");

        HttpEntity<CalculateFeeRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<ShippingFeeResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        ShippingFeeResponse.class
                );
        return response.getBody();
    }

}
