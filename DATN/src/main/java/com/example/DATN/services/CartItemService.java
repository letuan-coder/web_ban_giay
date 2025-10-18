//package com.example.DATN.services;
//
//import com.example.DATN.models.CartItem;
//import com.example.DATN.repositories.CartItemRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.Optional;
//
///**
// * Service nghiệp vụ sản phẩm trong giỏ hàng
// */
//@Service
//public class CartItemService {
//    @Autowired
//    private CartItemRepository cartItemRepository;
//
//    public List<CartItem> getAllCartItems() {
//        return cartItemRepository.findAll();
//    }
//
//    public Optional<CartItem> getCartItemById(Long id) {
//        return cartItemRepository.findById(id);
//    }
//
//    public CartItem createCartItem(CartItem cartItem) {
//        return cartItemRepository.save(cartItem);
//    }
//
//    public CartItem updateCartItem(CartItem cartItem) {
//        return cartItemRepository.save(cartItem);
//    }
//
//    public void deleteCartItem(Long id) {
//        cartItemRepository.deleteById(id);
//    }
//}
//
