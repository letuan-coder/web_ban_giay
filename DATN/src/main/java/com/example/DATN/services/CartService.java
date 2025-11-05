package com.example.DATN.services;

import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.dtos.respone.cart.CartResponse;
import com.example.DATN.dtos.respone.product.ProductVariantResponse;
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
import com.example.DATN.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service nghiệp vụ giỏ hàng
 */
@Service
@EnableWebSecurity
@RequiredArgsConstructor
public class CartService {

    final private CartRepository cartRepository;
    final private CartMapper cartMapper;
    final private RedisTemplate redisTemplate;
    final private UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final ObjectMapper objectMapper;
    private String CART_REDIS_KEY_PREFIX = "cart:user:";
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final GetJwtIdForGuest getJwtIdForGuest;

    @Transactional
    public CartResponse getCartByUserId() {
//       //clear toàn bộ cache redis
//       redisTemplate.getConnectionFactory().getConnection().flushAll();
        String guestKey = getJwtIdForGuest.GetGuestKey();
        if (guestKey != null && !guestKey.isEmpty()) {
            String redisGuestKey = CART_REDIS_KEY_PREFIX + guestKey;
            Object cachedGuestData = redisTemplate.opsForValue().get(redisGuestKey);
            if (cachedGuestData != null) {
                return objectMapper.convertValue(cachedGuestData, CartResponse.class);
            }
        }

        User user = getUserByJwtHelper.getCurrentUser();
        String redisUserKey = CART_REDIS_KEY_PREFIX+ user.getId();
        Object cachedUserData = redisTemplate.opsForValue().get(redisUserKey);
        if (cachedUserData != null) {
            return objectMapper.convertValue(cachedUserData, CartResponse.class);
        }

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setTotal_price(BigDecimal.ZERO);
            newCart.setItems(null);
            return cartRepository.save(newCart);
        });

        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        List<CartItemResponse> cartItemResponse = cartItemRepository
                .findByCart(cart).stream().map(cartItemMapper::toCartItemResponse).toList();
        BigDecimal total = Calculate_Total_Price(cartItemResponse);
        cartResponse.setTotal_price(total);

        // Cache the cart for the user
        redisTemplate.opsForValue().set(redisUserKey, cartResponse, 7, TimeUnit.DAYS);

        return cartResponse;
    }

    public CartResponse getCartById(UUID id) {
        Cart respone = cartRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        return cartMapper.toCartResponse(respone);
    }

    @Transactional(rollbackOn = Exception.class)
    public CartResponse createCartForUser() {
        User user = getUserByJwtHelper.getCurrentUser();
        // Nếu user đã có cart thì trả về cart đó luôn
        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setCreatedAt(LocalDateTime.now());
            cart.setTotal_price(BigDecimal.ZERO);
            cart = cartRepository.save(cart);
        }
        // Lưu cache vào Redis

        CartResponse dto = cartMapper.toCartResponse(cart);
        String redisUserKey = CART_REDIS_KEY_PREFIX+ user.getId(); // Consistent key
        redisTemplate.opsForValue().set(redisUserKey, dto, 7, TimeUnit.DAYS); // Add expiration
        return cartMapper.toCartResponse(cart);
    }

    @Async
    public BigDecimal Calculate_Total_Price(List<CartItemResponse> responses) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItemResponse response : responses) {
            // Lấy thông tin product variant từ response
            ProductVariantResponse variant = response.getProductVariant();
            BigDecimal price = variant.getDiscountPrice() != null
                    ? variant.getDiscountPrice()
                    : variant.getPrice();
            BigDecimal quantity = BigDecimal.valueOf(response.getQuantity());
            totalPrice = totalPrice.add(price.multiply(quantity));
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

