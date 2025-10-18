//package com.example.DATN.services;
//
//import com.example.DATN.constant.OrderStatus;
//import com.example.DATN.dtos.request.OrderItemRequest;
//import com.example.DATN.dtos.request.OrderRequest;
//import com.example.DATN.dtos.respone.OrderResponse;
//import com.example.DATN.exception.ApplicationException;
//import com.example.DATN.exception.ErrorCode;
//import com.example.DATN.mapper.OrderMapper;
//import com.example.DATN.models.*;
//import com.example.DATN.repositories.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//    private final UserRepository userRepository;
//    private final ProductVariantRepository productVariantRepository;
//    private final PaymentMethodRepository paymentMethodRepository;
//    private final OrderMapper orderMapper;
//    private final OrderItemRepository orderItemRepository;
//
//    @Transactional
//    public OrderResponse createOrder(OrderRequest request) {
//        User user = userRepository.findById(request.getUserId())
//                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
//
//        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
//                .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
//
//        Order order = new Order();
//        order.setUser(user);
//        order.setPaymentMethod(paymentMethod);
//        order.setOrderStatus(OrderStatus.PENDING);
//
//        Order savedOrder = orderRepository.save(order);
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        for (OrderItemRequest itemRequest : request.getItems()) {
//            ProductVariant productVariant = productVariantRepository.findById(itemRequest.getProductVariantId())
//                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
//
//            if (productVariant.getQuantity() < itemRequest.getQuantity()) {
//                throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK);
//            }
//
//            productVariant.setQuantity(productVariant.getQuantity() - itemRequest.getQuantity());
//            productVariantRepository.save(productVariant);
//
//            OrderItem orderItem = new OrderItem();
//            orderItem.setOrder(savedOrder);
//            orderItem.setProductVariant(productVariant);
//            orderItem.setQuantity(itemRequest.getQuantity());
//            orderItem.setPrice(productVariant.getPrice());
//            orderItems.add(orderItemRepository.save(orderItem));
//        }
//
//        savedOrder.setItems(orderItems);
//        return orderMapper.toOrderResponse(orderRepository.save(savedOrder));
//    }
//
//    public List<OrderResponse> getAllOrders() {
//        return orderRepository.findAll().stream()
//                .map(orderMapper::toOrderResponse)
//                .collect(Collectors.toList());
//    }
//
//    public OrderResponse getOrderById(Long id) {
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
//        return orderMapper.toOrderResponse(order);
//    }
//
//    @Transactional
//    public void deleteOrder(Long id) {
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
//
//        // Rollback stock quantity
//        for (OrderItem item : order.getItems()) {
//            ProductVariant productVariant = item.getProductVariant();
//            productVariant.setQuantity(productVariant.getQuantity() + item.getQuantity());
//            productVariantRepository.save(productVariant);
//        }
//
//        orderRepository.delete(order);
//    }
//
//    @Transactional
//    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
//        order.setOrderStatus(status);
//        return orderMapper.toOrderResponse(orderRepository.save(order));
//    }
//}