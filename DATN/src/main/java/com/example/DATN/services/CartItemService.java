package com.example.DATN.services;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.constant.VariantType;
import com.example.DATN.dtos.request.cart.CartItemRequest;
import com.example.DATN.dtos.request.cart.UpdateCartIItemRequest;
import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.CartItemMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private final ProductViewRepository productViewRepository;

    public List<CartItemResponse> getAllCartItems() {
//        String guestKey = getJwtIdForGuest.GetGuestKey();
//        if (guestKey != null && !guestKey.isEmpty()) {
//            String redisGuestKey = CART_REDIS_KEY_PREFIX + guestKey + ":items";
//            Object cachedGuestData = redisTemplate.opsForValue().get(redisGuestKey);
//            if (cachedGuestData != null) {
//                return objectMapper.convertValue(
//                        cachedGuestData,
//                        new TypeReference<List<CartItemResponse>>() {
//                        }
//                );
//            }
//        }
        User user = getUserByJwtHelper.getCurrentUser();
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));

        List<CartItemResponse> listResponse = cartItemRepository.findByCartOrderByCreatedAt(cart).stream()
                .map(cartItemMapper::toCartItemResponse).toList();
        return listResponse;
    }

    public CartItemResponse getCartItemById(UUID id) {
        var res = cartItemRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
        return cartItemMapper.toCartItemResponse(res);
    }

    @Async
    public void increaseView(UUID variantId, UUID product) {
        String day = DAY_FMT.format(Instant.now());
        String key = "product:view:variant:" + variantId + ":" + day;
        String keyProduct = "product:view:product:" + product + ":" + day;
        redisTemplate.opsForValue().increment(keyProduct, 1);
        redisTemplate.expire(keyProduct, Duration.ofDays(2));
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, Duration.ofDays(2));
    }

    @Async
    public void AddView(ProductVariant variant, User user) {
        UUID productId = variant.getProductColor().getProduct().getId();
        ProductView view = ProductView.builder()
                .variant(variant)
                .variantType(VariantType.CART)
                .user(user)
                .build();
        productViewRepository.save(view);
        increaseView(variant.getId(), productId);
    }

    @Transactional
    public CartItemResponse AddCartItem(CartItemRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();

        Cart cart = new Cart();
        cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        ProductVariant variant = productVariantRepository
                .findBysku(request.getSku())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        AddView(variant, user);
        if (variant.getIsAvailable() == Is_Available.NOT_AVAILABLE) {
            throw new ApplicationException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }

        CartItem cartItem = null;
        List<CartItemResponse> currentCartItemResponses = new ArrayList<>();
        Optional<CartItem> existingItemOptional = cartItemRepository.findByCartAndProductVariant(cart, variant);
        if (existingItemOptional.isPresent()) {
            cartItem = existingItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(variant);
            cartItem.setQuantity(request.getQuantity());
        }
        cartItem.setTotal_price(cartItem.getProductVariant().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        cartItemRepository.save(cartItem);
        List<CartItem> updatedDbCartItems = cartItemRepository.findByCartOrderByCreatedAt(cart);
        BigDecimal newTotalPrice = updatedDbCartItems.stream()
                .map(item -> item.getProductVariant().getPrice()
                        .multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotal_price(newTotalPrice);
        cartRepository.save(cart);
        return cartItemMapper.toCartItemResponse(cartItem);
    }

    @Transactional(rollbackOn = Exception.class)
    public void updateCartItem(UpdateCartIItemRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        if (!cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            ProductVariant productVariant = productVariantRepository.findBysku(request.getSku())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
            Optional<CartItem> existingItemOpt = cartItemRepository
                    .findByProductVariantAndCart(productVariant, cart);
            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(request.getQuantity());
                existingItem.setTotal_price(
                        existingItem.getProductVariant()
                                .getPrice().multiply
                                        (BigDecimal.valueOf(request.getQuantity()))
                );
                cartItemRepository.save(existingItem);
            }
        } else {
            CartItemRequest cartItemRequest = CartItemRequest.builder()
                    .sku(request.getSku())
                    .quantity(request.getQuantity())
                    .build();
            AddCartItem(cartItemRequest);
        }
//        return cartItemMapper.toCartItemResponse(savedItem);

    }

    public void deleteCartItem(List<UUID> id) {
        User user = getUserByJwtHelper.getCurrentUser();
        Optional<Cart> cartOtp = cartRepository.findByUser(user);
        if (cartOtp.isPresent()) {
            List<CartItem> cartItem = cartItemRepository.findAllById(id);
            cartItemRepository.deleteAll(cartItem);
        } else {
            Cart cart = Cart.builder()
                    .items(null)
                    .total_price(BigDecimal.ZERO)
                    .user(user)
                    .build();
            cartRepository.save(cart);
        }
    }
}

