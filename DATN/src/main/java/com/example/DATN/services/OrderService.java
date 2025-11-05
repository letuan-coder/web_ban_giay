package com.example.DATN.services;

import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.order.OrderRespone;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderItemMapper;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.models.Order;
import com.example.DATN.models.OrderItem;
import com.example.DATN.models.ProductColor;
import com.example.DATN.models.User;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final ProductColorRepository productColorRepository;

    public OrderRespone createOrder(OrderRequest orderRequest) {
        User user = getUserByJwtHelper.getCurrentUser();
        Order order = orderMapper.toEntity(orderRequest);
        order.setUser(user);
        order.setPaymentMethod(paymentMethodRepository.findById(orderRequest.getPaymentMethodId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)));

        List<OrderItem> orderItems = orderRequest.getItems().stream().map(itemRequest -> {
            List<ProductColor> productColors  = productColorRepository.findAllById(orderRequest.getItems().stream()
                    .map(OrderItemRequest::getProductColorId).toList());
            ProductColor productColor = productColors.stream()
                    .filter(p -> p.getId().equals(itemRequest.getProductColorId()))
                    .findFirst()
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
            OrderItem orderItem = orderItemMapper.toOrderItem(itemRequest);
            orderItem.setOrder(order);
            orderItem.setProductColor(productColor) ;
            return orderItem;
        }).collect(Collectors.toList());
        order.setItems(orderItems);
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        return orderMapper.toResponse(order);
    }

    public List<OrderRespone> getOrdersByUser() {
        User user =  getUserByJwtHelper.getCurrentUser();
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream().map(orderMapper::toResponse).collect(Collectors.toList());
    }

    public OrderRespone getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toResponse(order);
    }
}
