package com.example.DATN.services;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.request.CartItemRequest;
import com.example.DATN.dtos.request.UpdateCartIItemRequest;
import com.example.DATN.dtos.respone.CartItemResponse;
import com.example.DATN.dtos.respone.CartResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.CartItemMapper;
import com.example.DATN.mapper.CartMapper;
import com.example.DATN.models.Cart;
import com.example.DATN.models.CartItem;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.User;
import com.example.DATN.repositories.CartItemRepository;
import com.example.DATN.repositories.CartRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartService cartService;

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = jwt.getSubject();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
    }

    public List<CartItemResponse> getAllCartItems() {
        return cartItemRepository
                .findAll().stream()
                .map(cartItemMapper::toCartItemResponse).toList();
    }

    public CartItemResponse getCartItemById(UUID id) {
        var res = cartItemRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
        return cartItemMapper.toCartItemResponse(res);
    }
    private final CartMapper cartMapper;

    @Transactional
    public CartItemResponse AddCartItem(CartItemRequest request) {
        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        ProductVariant variant = productVariantRepository
                .findById(request.getProductVariantId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        if (variant.getIsAvailable() == Is_Available.NOT_AVAILABLE) {
            throw new ApplicationException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }

        if (variant.getStock() < request.getQuantity()) {
            throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
        }

        Optional<CartItem> existingItemOptional = cartItemRepository.findByCartAndProductVariant(cart, variant);

        CartItem cartItem;
        if (existingItemOptional.isPresent()) {
            cartItem = existingItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setTotal_price(cartItem.getProductVariant().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(variant);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setTotal_price(cartItem.getProductVariant().getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));

        }

        cartItemRepository.save(cartItem);

        // Recalculate total price
        BigDecimal newTotalPrice = cart.getItems().stream()
                .map(item -> item.getProductVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotal_price(newTotalPrice);
        cartRepository.save(cart);

        // Update Redis cache
        String redisKey = "cart:user:" + user.getId();
        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        redisTemplate.opsForValue().set(redisKey, cartResponse, 7, TimeUnit.DAYS);

        return cartItemMapper.toCartItemResponse(cartItem);
    }

    public CartItemResponse updateCartItem(UpdateCartIItemRequest req) {
        CartItem existingItem = cartItemRepository
                .findById(req.getId())
                .orElseThrow(()->new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
        existingItem.setQuantity(req.getQuantity());
        // Khi cập nhật cũng cần tính lại tổng tiền và xóa cache
        CartItem savedItem = cartItemRepository.save(existingItem);
        Cart cart = savedItem.getCart();
        BigDecimal newTotalPrice = cart.getItems().stream()
                .map(item -> item.getProductVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotal_price(newTotalPrice);
        cartRepository.save(cart);

        redisTemplate.delete("cart:user:" + cart.getUser().getId());

        return cartItemMapper.toCartItemResponse(savedItem);
    }

    public void deleteCartItem(UUID id) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
        Cart cart = cartItem.getCart();
        cartItemRepository.deleteById(id);

        // Tính lại tổng tiền và xóa cache sau khi xóa
        BigDecimal newTotalPrice = cart.getItems().stream()
                .filter(item -> !item.getId().equals(id))
                .map(item -> item.getProductVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotal_price(newTotalPrice);
        cartRepository.save(cart);

        redisTemplate.delete("cart:user:" + cart.getUser().getId());
    }
}

