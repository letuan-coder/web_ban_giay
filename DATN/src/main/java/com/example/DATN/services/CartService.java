//package com.example.DATN.services;
//
//import com.example.DATN.models.Cart;
//import com.example.DATN.repositories.CartRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
///**
// * Service nghiệp vụ giỏ hàng
// */
//@Service
//public class CartService {
//    @Autowired
//    private CartRepository cartRepository;
//
//    public List<Cart> getAllCarts() {
//        return cartRepository.findAll();
//    }
//
//    public Optional<Cart> getCartById(UUID id) {
//        return cartRepository.findById(id);
//    }
//
//    public Cart createCart(Cart cart) {
//        return cartRepository.save(cart);
//    }
//
//    public Cart updateCart(Cart cart) {
//        return cartRepository.save(cart);
//    }
//
//    public void deleteCart(UUID id) {
//        cartRepository.deleteById(id);
//    }
//}
//
