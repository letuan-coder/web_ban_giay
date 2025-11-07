package com.example.DATN.services;

import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.dtos.respone.cart.CartResponse;
import com.example.DATN.dtos.respone.cart.GuestCartResponse;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                .findByCart(cart).stream().map(cartItemMapper::toCartItemResponse).toList();
        BigDecimal total = Calculate_Total_Price(cartItemResponse);
        cartResponse.setTotal_price(total);
        return cartResponse;
    }

    public CartResponse getCartById(UUID id) {
        Cart respone = cartRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        return cartMapper.toCartResponse(respone);
    }

    public Cart MergeCartForUser(User user) {
        String guest_key = "cart:" + getJwtIdForGuest.GetGuestKey() + ":item";
        Object cache = redisTemplate.opsForValue().get(guest_key);
        if(cache==null){
            Cart newCart =  Cart.builder()
                    .user(user)
                    .total_price(BigDecimal.ZERO)
                    .items(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);

        }
        else {
            GuestCartResponse guestCartResponse =
                    objectMapper.convertValue(cache, new TypeReference<GuestCartResponse>() {
                    });
            List<CartItemResponse> listItem = new ArrayList<>();
            for (CartItemResponse response : guestCartResponse.getCartItems()) {
                listItem.add(response);
            }
            CartResponse newCartForUser = CartResponse.builder()
                    .cartItems(listItem)
                    .userId(user.getId())
                    .total_price(guestCartResponse.getTotal_price())
                    .build();
            Cart cart = cartMapper.toEntity(newCartForUser);
            return cartRepository.save(cart);
        }
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

