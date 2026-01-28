package com.example.DATN.services;

import com.example.DATN.dtos.respone.PromotionPriceResponse;
import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.dtos.respone.cart.CartResponse;
import com.example.DATN.dtos.respone.cart.ProductCartItemResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetJwtIdForGuest;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.CartItemMapper;
import com.example.DATN.mapper.CartMapper;
import com.example.DATN.models.Cart;
import com.example.DATN.models.User;
import com.example.DATN.repositories.CartItemRepository;
import com.example.DATN.repositories.CartRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@EnableWebSecurity
@RequiredArgsConstructor
public class CartService {

    final private CartRepository cartRepository;
    final private CartMapper cartMapper;
    final private RedisTemplate redisTemplate;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final ObjectMapper objectMapper;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final GetJwtIdForGuest getJwtIdForGuest;

    @Transactional
    public CartResponse getCartByUserId() throws JsonProcessingException {
        User user = getUserByJwtHelper.getCurrentUser();
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setTotal_price(BigDecimal.ZERO);
            newCart.setItems(null);
            return cartRepository.save(newCart);
        });
        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        List<CartItemResponse> cartItemResponse = cartItemRepository
                .findByCartOrderByCreatedAt(cart).stream().map(cartItemMapper::toCartItemResponse).toList();
        cartItemResponse = calculatePromoPrice(cartItemResponse);
        cartResponse.setCartItems(cartItemResponse);
        BigDecimal total = Calculate_Total_Price(cartItemResponse);
        cartResponse.setTotal_price(total);

        return cartResponse;
    }

    public List<CartItemResponse> getPromotionsBySkus(
            List<String> skus, List<CartItemResponse> response
    ) {
        List<String> keys = skus.stream()
                .map(sku -> "PROMO:VARIANT:" + sku)
                .toList();

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        Map<String, PromotionPriceResponse> responseMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            Object value = values.get(i);
            if (value == null) continue;

            try {
                PromotionPriceResponse promo =
                        objectMapper.readValue(
                                value.toString(),
                                PromotionPriceResponse.class
                        );
                String sku = skus.get(i);
                responseMap.put(sku, promo);

            } catch (Exception ignored) {

            }
        }
        for (CartItemResponse variant : response) {
            PromotionPriceResponse promo = responseMap.get(variant.getProductVariant().getSku());
            if (promo != null) {
                variant.getProductVariant().setPrice(promo.getOriginalPrice());
                variant.getProductVariant().setDiscountPrice(promo.getDiscountPrice());

            }
        }
        return response;
    }

    public List<CartItemResponse> calculatePromoPrice(
            List<CartItemResponse> cartItemResponses
    ) throws JsonProcessingException {
        List<ProductCartItemResponse> variantResponseList = new ArrayList<>();
        List<String> skus = cartItemResponses.stream().map(item -> item.getProductVariant().getSku()).toList();
       return getPromotionsBySkus(skus, cartItemResponses);
    }

    public CartResponse getCartById(UUID id) {
        Cart respone = cartRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        return cartMapper.toCartResponse(respone);
    }


    @Transactional(rollbackOn = Exception.class)
    public CartResponse createCartForUser() {
        User user = getUserByJwtHelper.getCurrentUser();
        Cart cart = user.getCart();
        if (cart == null) {
            cart = Cart.builder()
                    .user(user)
                    .total_price(BigDecimal.ZERO)
                    .build();
            cart = cartRepository.save(cart);
        }
        return cartMapper.toCartResponse(cart);
    }

    @Async
    public BigDecimal Calculate_Total_Price(List<CartItemResponse> responses) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItemResponse response : responses) {
            ProductCartItemResponse variant = response.getProductVariant();
            BigDecimal variantPrice = variant.getPrice();
            if (response.getProductVariant().getDiscountPrice().equals(BigDecimal.ZERO)){
               variantPrice = response.getProductVariant().getDiscountPrice();
            }
            BigDecimal quantity = BigDecimal.valueOf(response.getQuantity());
            totalPrice = totalPrice.add(variantPrice.multiply(quantity));
        }
        return totalPrice;
    }

    public Cart updateCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public void deleteCart(UUID id) {
        cartRepository.deleteById(id);
    }

}

