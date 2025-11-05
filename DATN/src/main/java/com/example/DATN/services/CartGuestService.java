package com.example.DATN.services;


import com.example.DATN.dtos.respone.cart.GuestCartResponse;
import com.example.DATN.helper.GetJwtIdForGuest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class CartGuestService {
    private final ObjectMapper objectMapper;
    private final GetJwtIdForGuest getJwtIdForGuest;
    private RedisTemplate redisTemplate;

    public GuestCartResponse getCart() {
        String guest_key = "cart:" + getJwtIdForGuest.GetGuestKey()+":item";
        Object cache = redisTemplate.opsForValue().get(guest_key);
        GuestCartResponse guestCartResponse = new GuestCartResponse();
        if (cache == null) {
            guestCartResponse = GuestCartResponse.builder()
                    .guest_key(guest_key)
                    .total_price(BigDecimal.ZERO)
                    .cartItems(null)
                    .build();
            redisTemplate.opsForValue().set(guest_key, guestCartResponse, 7, TimeUnit.DAYS);
        }
        else
            return objectMapper.convertValue(cache, new TypeReference<GuestCartResponse>() {});
        return guestCartResponse;
    }

    public GuestCartResponse createCartForGuest() {
        String guest_key = "cart:" + getJwtIdForGuest.GetGuestKey();
        Object cache = redisTemplate.opsForValue().get(guest_key);
        GuestCartResponse guestCartResponse;
        if (cache == null) {
            guestCartResponse = GuestCartResponse.builder()
                    .guest_key(guest_key)
                    .total_price(BigDecimal.ZERO)
                    .cartItems(null)
                    .build();
            redisTemplate.opsForValue().set(guest_key, guestCartResponse, 7, TimeUnit.DAYS);
        } else {
            guestCartResponse = objectMapper.convertValue(cache, GuestCartResponse.class);
        }
        return guestCartResponse;
    }
}
