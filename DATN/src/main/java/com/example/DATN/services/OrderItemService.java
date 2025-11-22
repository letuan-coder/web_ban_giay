package com.example.DATN.services;

import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.respone.order.OrderItemResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.OrderItemMapper;
import com.example.DATN.models.Order;
import com.example.DATN.models.OrderItem;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;
    private final ProductColorRepository productColorRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public OrderItemResponse addOrderItemToOrder(Long orderId, OrderItemRequest itemRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        ProductVariant variant= productVariantRepository.findById(itemRequest.getProductVariantId())
                .orElseThrow(()->new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        OrderItem orderItem = orderItemMapper.toOrderItem(itemRequest);
        orderItem.setOrder(order);
        orderItem.setProductVariant(variant);
        orderItem.setQuantity(itemRequest.getQuantity());
        order.getItems().add(orderItem);
        updateOrderTotalPrice(order);
        orderItemRepository.save(orderItem);
        orderRepository.save(order);

        return orderItemMapper.toOrderItemResponse(orderItem);
    }

    @Transactional
    public OrderItemResponse updateOrderItemQuantity(Long itemId, int quantity) {
        OrderItem orderItem = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        orderItem.setQuantity(quantity);
        updateOrderTotalPrice(orderItem.getOrder());
        orderItemRepository.save(orderItem);

        return orderItemMapper.toOrderItemResponse(orderItem);
    }

    @Transactional
    public void removeOrderItem(Long itemId) {
        OrderItem orderItem = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        Order order = orderItem.getOrder();
        order.getItems().remove(orderItem);
        updateOrderTotalPrice(order);

        orderItemRepository.delete(orderItem);
        orderRepository.save(order);
    }

    public OrderItemResponse getOrderItemById(Long itemId) {
        OrderItem orderItem = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        return orderItemMapper.toOrderItemResponse(orderItem);
    }

    private void updateOrderTotalPrice(Order order) {
        BigDecimal totalPrice = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal_price(totalPrice);
    }
}