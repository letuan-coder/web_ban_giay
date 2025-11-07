package com.example.DATN.services;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.request.cart.CartItemRequest;
import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.dtos.respone.cart.GuestCartResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetJwtIdForGuest;
import com.example.DATN.mapper.CartItemMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.repositories.CartItemRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class CartItemGuestService {
    private final CartGuestService cartGuestService;
    private final RedisTemplate redisTemplate;
    private final CartItemMapper cartItemMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductVariantRepository productVariantRepository;
    private final ObjectMapper objectMapper;
    private final CartItemRepository cartItemRepository;
    private final GetJwtIdForGuest getJwtIdForGuest;


    public GuestCartResponse AddGuestItemToCart(CartItemRequest request) {
        String guest_key = "cart:" + getJwtIdForGuest.GetGuestKey() + ":item";
        GuestCartResponse response = cartGuestService.getCart();
        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        List<CartItemResponse> listItem = response.getCartItems();
        if (variant.getIsAvailable() == Is_Available.NOT_AVAILABLE) {
            throw new ApplicationException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        } else {
            if (request.getQuantity() > variant.getStock()) {
                throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
            }
            for (CartItemResponse responseItem: listItem) {
                if (responseItem.getProductVariant().getId().equals(request.getProductVariantId())) {
                    responseItem.setQuantity(responseItem.getQuantity()+request.getQuantity());
                }
            }
            response.setCartItems(listItem);
            response.setGuest_key(guest_key);
            if (variant.getDiscountPrice() == null) {
                BigDecimal newTotalPrice = response.getCartItems().stream()
                        .map(itemCart -> itemCart.getProductVariant().getPrice().multiply(new BigDecimal(itemCart.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                response.setTotal_price(newTotalPrice);
            } else {
                BigDecimal newTotalPrice = response.getCartItems().stream()
                        .map(Itemcart -> Itemcart.getProductVariant().getDiscountPrice().multiply(new BigDecimal(Itemcart.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                response.setTotal_price(newTotalPrice);
            }
            redisTemplate.opsForValue().set(guest_key, response, 7, TimeUnit.DAYS);
            return response;
        }
    }
}
