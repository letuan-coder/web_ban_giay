package com.example.DATN.services;

import com.example.DATN.dtos.respone.CartItemResponse;
import com.example.DATN.dtos.respone.CartResponse;
import com.example.DATN.dtos.respone.ProductVariantResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.CartItemMapper;
import com.example.DATN.mapper.CartMapper;
import com.example.DATN.models.Cart;
import com.example.DATN.models.CartItem;
import com.example.DATN.models.User;
import com.example.DATN.repositories.CartItemRepository;
import com.example.DATN.repositories.CartRepository;
import com.example.DATN.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private static final String CART_KEY_PREFIX = "cart:";

    public Page<CartResponse> getAllCarts(Pageable pageable) {
        return cartRepository.findAll(pageable)
                .map(cartMapper::toCartResponse);
    }

    public User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = jwt.getSubject(); // "admin"

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public CartResponse getCartByUserId() {
//       //clear toàn bộ cache redis
//       redisTemplate.getConnectionFactory().getConnection().flushAll();
        User user = getCurrentUser();
        String key = "cart:user:" + user.getId();
//         1. Lấy dữ liệu giỏ hàng từ Redis
        Object cachedData = redisTemplate.opsForValue().get(key);
        if (cachedData != null) {
            // Nếu có trong cache, chuyển đổi và trả về
            return objectMapper.convertValue(cachedData, CartResponse.class);
        }

        // 2. Nếu không có trong cache, tìm trong DB hoặc tạo mới
        // orElseGet sẽ thực thi lambda để tạo cart mới nếu không tìm thấy
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            List<CartItem> cartItems = null;
            newCart.setUser(user);
            newCart.setTotal_price(BigDecimal.ZERO);
            newCart.setItems(cartItems);// Khởi tạo tổng tiền là 0
            // createdAt và updatedAt nên được tự động quản lý bởi @CreationTimestamp/@UpdateTimestamp trong BaseEntity
            return cartRepository.save(newCart);
        });

        // 3. Chuyển đổi sang DTO
        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        List<CartItemResponse> cartItemResponse = (cartItemRepository.findByCart(cart).stream().map(cartItemMapper::toCartItemResponse).toList());
        BigDecimal total= Calculate_Total_Price(cartItemResponse);
        cartResponse.setTotal_price(total);
        // 4. Lưu vào Redis cho những lần gọi sau
        redisTemplate.opsForValue().set(key, cartResponse, 7, TimeUnit.DAYS);

        return cartResponse;
    }

    public CartResponse getCartById(UUID id) {
        Cart respone = cartRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        return cartMapper.toCartResponse(respone);
    }

    @Transactional(rollbackOn = Exception.class)
    public CartResponse createCart() {
        User user = getCurrentUser();
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

        redisTemplate.opsForValue().set("cart:" + cart.getId(), dto);
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

