package com.example.DATN.repositories;

import com.example.DATN.models.Cart;
import com.example.DATN.models.CartItem;
import com.example.DATN.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu sản phẩm trong giỏ hàng
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByProductVariant(ProductVariant variant);

    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant variant);

}

