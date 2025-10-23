package com.example.DATN.services;

import com.example.DATN.models.OrderItem;
import com.example.DATN.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service nghiệp vụ sản phẩm trong đơn hàng
 */
@Service
@RequiredArgsConstructor
public class OrderItemService {

    final private OrderItemRepository orderItemRepository;

    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    public Optional<OrderItem> getOrderItemById(Long id) {
        return orderItemRepository.findById(id);
    }

    public OrderItem createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public OrderItem updateOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public void deleteOrderItem(Long id) {
        orderItemRepository.deleteById(id);
    }
}

