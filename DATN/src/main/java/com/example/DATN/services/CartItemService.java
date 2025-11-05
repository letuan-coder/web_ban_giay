package com.example.DATN.services;

import com.example.DATN.constant.Is_Available;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.DATN.dtos.request.cart.CartItemRequest;
import com.example.DATN.dtos.request.cart.UpdateCartIItemRequest;
import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.dtos.respone.cart.CartResponse;
import com.example.DATN.dtos.respone.cart.GuestCartResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetJwtIdForGuest;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.CartItemMapper;
import com.example.DATN.mapper.CartMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.Cart;
import com.example.DATN.models.CartItem;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.User;
import com.example.DATN.repositories.CartItemRepository;
import com.example.DATN.repositories.CartRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final ProductVariantMapper productVariantMapper;
    private String CART_REDIS_KEY_PREFIX = "cart:";
    private final GetJwtIdForGuest getJwtIdForGuest;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final ObjectMapper objectMapper;

    public List<CartItemResponse> getAllCartItems() {
        String guestKey = getJwtIdForGuest.GetGuestKey();
        if (guestKey != null && !guestKey.isEmpty()) {
            String redisGuestKey = CART_REDIS_KEY_PREFIX + guestKey + ":items";
            Object cachedGuestData = redisTemplate.opsForValue().get(redisGuestKey);
            if (cachedGuestData != null) {
                return objectMapper.convertValue(
                        cachedGuestData,
                        new TypeReference<List<CartItemResponse>>() {}
                );
            }
        }

        User user = getUserByJwtHelper.getCurrentUser();
        String redisUserKey = "cart:user:" + user.getId() + ":items";
        Object cachedUserData = redisTemplate.opsForValue().get(redisUserKey);
        if (cachedUserData != null) {
            return objectMapper.convertValue(
                    cachedUserData,
                    new TypeReference<List<CartItemResponse>>() {}
            );
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_NOT_FOUND));
        List<CartItemResponse> listResponse = cartItemRepository.findByCart(cart).stream()
                .map(cartItemMapper::toCartItemResponse).toList();

        redisTemplate.opsForValue().set(redisUserKey, listResponse, 7, TimeUnit.DAYS);

        return listResponse;
    }

    public GuestCartResponse GuestCartItemForGuest() {
        String guest_key = CART_REDIS_KEY_PREFIX + getJwtIdForGuest.GetGuestKey();
        Object cacheValue = redisTemplate.opsForValue().get(guest_key);

        GuestCartResponse guestCartResponse = GuestCartResponse.builder()
                .guest_key(guest_key)
                .total_price(BigDecimal.ZERO)
                .cartItems(null)
                .build();
        redisTemplate.opsForValue().set(guest_key, guestCartResponse, 7, TimeUnit.DAYS);

        return guestCartResponse;

    }

    public CartItemResponse getCartItemById(UUID id) {
        var res = cartItemRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
        return cartItemMapper.toCartItemResponse(res);
    }

    private final CartMapper cartMapper;

    @Transactional
    public CartItemResponse AddCartItem(CartItemRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        String redisCartKey;
        String redisCartItemsKey;
        Cart cart = null;

        if (user != null) { // Authenticated user
            redisCartKey = "cart:user:" + user.getId();
            redisCartItemsKey = "cart:user:" + user.getId() + ":items";
            cart = cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(user);
                        return cartRepository.save(newCart);
                    });
        } else { // Guest user
            String guestKey = getJwtIdForGuest.GetGuestKey();
            if (guestKey == null || guestKey.isEmpty()) {
                throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
            }
            redisCartKey = CART_REDIS_KEY_PREFIX + guestKey;
            redisCartItemsKey = CART_REDIS_KEY_PREFIX + guestKey + ":items";
        }

        ProductVariant variant = productVariantRepository
                .findById(request.getProductVariantId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        if (variant.getIsAvailable() == Is_Available.NOT_AVAILABLE) {
            throw new ApplicationException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        if (variant.getStock() < request.getQuantity()) {
            throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
        }

        CartItem cartItem = null;
        List<CartItemResponse> currentCartItemResponses = new ArrayList<>();

        if (user != null) {
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
            cartItem.setTotal_price(cartItem.getProductVariant().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemRepository.save(cartItem);

            // Recalculate total price for the cart from DB items
            List<CartItem> updatedDbCartItems = cartItemRepository.findByCart(cart);
            BigDecimal newTotalPrice = updatedDbCartItems.stream()
                    .map(item -> item.getProductVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            cart.setTotal_price(newTotalPrice);
            cartRepository.save(cart);

            redisTemplate.delete(redisCartKey);
            redisTemplate.delete(redisCartItemsKey);

            return cartItemMapper.toCartItemResponse(cartItem);

        } else {
            Object cachedGuestItems = redisTemplate.opsForValue().get(redisCartItemsKey);
            if (cachedGuestItems != null) {
                currentCartItemResponses = objectMapper.convertValue(cachedGuestItems, new com.fasterxml.jackson.core.type.TypeReference<List<CartItemResponse>>() {
                });
            }

            Optional<CartItemResponse> existingItemResponseOptional = currentCartItemResponses.stream()
                    .filter(item -> item.getProductVariant().getId().equals(variant.getId()))
                    .findFirst();

            if (existingItemResponseOptional.isPresent()) {
                CartItemResponse existingResponse = existingItemResponseOptional.get();
                existingResponse.setQuantity(existingResponse.getQuantity() + request.getQuantity());
                cartItem = cartItemMapper.toEntity(existingResponse); // Convert back to CartItem for return
            } else {
                CartItemResponse newCartItemResponse = new CartItemResponse();
                newCartItemResponse.setProductVariant(productVariantMapper.toProductVariantResponse(variant));
                newCartItemResponse.setQuantity(request.getQuantity());
                currentCartItemResponses.add(newCartItemResponse);
                cartItem = cartItemMapper.toEntity(newCartItemResponse); // Convert back to CartItem for return
            }

            redisTemplate.opsForValue().set(redisCartItemsKey, currentCartItemResponses, 7, TimeUnit.DAYS);

            CartResponse guestCartResponse = new CartResponse();
            guestCartResponse.setCartItems(currentCartItemResponses);
            BigDecimal guestTotalPrice = cartService.Calculate_Total_Price(currentCartItemResponses);
            guestCartResponse.setTotal_price(guestTotalPrice);
            redisTemplate.opsForValue().set(redisCartKey, guestCartResponse, 7, TimeUnit.DAYS);

            return cartItemMapper.toCartItemResponse(cartItem);
        }
    }

    public CartItemResponse updateCartItem(UpdateCartIItemRequest req) {
        User user = getUserByJwtHelper.getCurrentUser();
        if (user != null) { // Authenticated user
            CartItem existingItem = cartItemRepository
                    .findById(req.getId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
            existingItem.setQuantity(req.getQuantity());
            CartItem savedItem = cartItemRepository.save(existingItem);
            Cart cart = savedItem.getCart();
            BigDecimal newTotalPrice = cart.getItems().stream()
                    .map(item -> item.getProductVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            cart.setTotal_price(newTotalPrice);
            cartRepository.save(cart);

            // Invalidate cache for authenticated user
            redisTemplate.delete("cart:user:" + cart.getUser().getId());
            redisTemplate.delete("cart:user:" + cart.getUser().getId() + ":items");

            return cartItemMapper.toCartItemResponse(savedItem);
        } else { // Guest user
            String guestKey = getJwtIdForGuest.GetGuestKey();
            if (guestKey == null || guestKey.isEmpty()) {
                throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
            }
            String redisCartKey = CART_REDIS_KEY_PREFIX + guestKey;
            String redisCartItemsKey = CART_REDIS_KEY_PREFIX + guestKey + ":items";

            Object cachedGuestItems = redisTemplate.opsForValue().get(redisCartItemsKey);
            if (cachedGuestItems == null) {
                throw new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND); // Or handle as empty cart
            }
            List<CartItemResponse> currentCartItemResponses = objectMapper.convertValue(cachedGuestItems, new com.fasterxml.jackson.core.type.TypeReference<List<CartItemResponse>>() {
            });

            Optional<CartItemResponse> existingItemResponseOptional = currentCartItemResponses.stream()
                    .filter(item -> item.getId().equals(req.getId()))
                    .findFirst();

            if (existingItemResponseOptional.isEmpty()) {
                throw new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND);
            }

            CartItemResponse existingResponse = existingItemResponseOptional.get();
            existingResponse.setQuantity(req.getQuantity());

            // Update Redis cache for cart items
            redisTemplate.opsForValue().set(redisCartItemsKey, currentCartItemResponses, 7, TimeUnit.DAYS);

            // Update Redis cache for the main cart object (CartResponse)
            CartResponse guestCartResponse = new CartResponse();
            guestCartResponse.setCartItems(currentCartItemResponses);
            BigDecimal guestTotalPrice = cartService.Calculate_Total_Price(currentCartItemResponses);
            guestCartResponse.setTotal_price(guestTotalPrice);
            redisTemplate.opsForValue().set(redisCartKey, guestCartResponse, 7, TimeUnit.DAYS);

            return cartItemMapper.toCartItemResponse(cartItemMapper.toEntity(existingResponse));
        }
    }

    public void deleteCartItem(UUID id) {
        User user = getUserByJwtHelper.getCurrentUser();
        if (user != null) { // Authenticated user
            CartItem cartItem = cartItemRepository.findById(id)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
            Cart cart = cartItem.getCart();
            cartItemRepository.deleteById(id);

            // Recalculate total price and invalidate cache
            BigDecimal newTotalPrice = cart.getItems().stream()
                    .filter(item -> !item.getId().equals(id))
                    .map(item -> item.getProductVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            cart.setTotal_price(newTotalPrice);
            cartRepository.save(cart);

            redisTemplate.delete("cart:user:" + cart.getUser().getId());
            redisTemplate.delete("cart:user:" + cart.getUser().getId() + ":items");

        } else { // Guest user
            String guestKey = getJwtIdForGuest.GetGuestKey();
            if (guestKey == null || guestKey.isEmpty()) {
                throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
            }
            String redisCartKey = CART_REDIS_KEY_PREFIX + guestKey;
            String redisCartItemsKey = CART_REDIS_KEY_PREFIX + guestKey + ":items";

            Object cachedGuestItems = redisTemplate.opsForValue().get(redisCartItemsKey);
            if (cachedGuestItems == null) {
                throw new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND); // Or handle as empty cart
            }
            List<CartItemResponse> currentCartItemResponses = objectMapper.convertValue(cachedGuestItems, new com.fasterxml.jackson.core.type.TypeReference<List<CartItemResponse>>() {
            });

            boolean removed = currentCartItemResponses.removeIf(item -> item.getId().equals(id));
            if (!removed) {
                throw new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND);
            }

            // Update Redis cache for cart items
            redisTemplate.opsForValue().set(redisCartItemsKey, currentCartItemResponses, 7, TimeUnit.DAYS);

            // Update Redis cache for the main cart object (CartResponse)
            CartResponse guestCartResponse = new CartResponse();
            guestCartResponse.setCartItems(currentCartItemResponses);
            BigDecimal guestTotalPrice = cartService.Calculate_Total_Price(currentCartItemResponses);
            guestCartResponse.setTotal_price(guestTotalPrice);
            redisTemplate.opsForValue().set(redisCartKey, guestCartResponse, 7, TimeUnit.DAYS);
        }
    }
}

