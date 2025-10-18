package com.example.DATN.services;

import com.example.DATN.dtos.request.ghtk.GHTKRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor

public class GhtkServices {

    @Value("${ghtk.api.url}")
    private String API_URL ;
    @Value("${ghtk.api.token}")
    private String TOKEN;

    public String createOrder(GHTKRequest orderRequest) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", TOKEN);
            headers.forEach((k,v) -> System.out.println(k + ": " + v));
            HttpEntity<GHTKRequest> entity = new HttpEntity<>(orderRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi: " + e.getMessage();
        }
    }
}
