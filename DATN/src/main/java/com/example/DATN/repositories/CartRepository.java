package com.example.DATN.repositories;

import com.example.DATN.models.Cart;
import com.example.DATN.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu giỏ hàng
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findById (UUID id);
    Optional<Cart> findByUser(User user);
}

