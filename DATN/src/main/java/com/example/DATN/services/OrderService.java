package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.Is_Available;
import com.example.DATN.constant.OrderStatus;
import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.order.OrderRespone;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.models.Order;
import com.example.DATN.models.OrderItem;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.User;
import com.example.DATN.repositories.CartRepository;
import com.example.DATN.repositories.OrderRepository;
import com.example.DATN.repositories.PaymentMethodRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderMapper orderMapper;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional(rollbackOn = Exception.class)
    public OrderRespone createOrder(OrderRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        List<ProductVariant> listProductVariant =
                productVariantRepository.findAllById(request.getProductColorId()) ;
        Order order = new Order();
        Long orderId = snowflakeIdGenerator.nextId();
        order.setId(orderId);
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)));
        List<OrderItem> orderItems = new ArrayList<>();
        for (ProductVariant item : listProductVariant) {
            if(item.getIsAvailable()== Is_Available.NOT_AVAILABLE){
                throw new ApplicationException(ErrorCode.PRODUCT_NOT_AVAILABLE);
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProductVariant(item);
            orderItem.setPrice(item.getPrice());
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal
                                .valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal_price(total);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    public List<OrderRespone> getOrdersByUser() {
        User user = getUserByJwtHelper.getCurrentUser();
        List<Order> orders = orderRepository.findByUser(user);
       return orders.stream().map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrderRespone getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toResponse(order);
    }

    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    public void updateOrderStatus(Long orderId,
                                  String newStatus,
                                  String expectedOldStatus) {
        Order order = findOrderById(orderId);
        OrderStatus expectedStatusEnum = OrderStatus.valueOf(expectedOldStatus.toUpperCase());
        OrderStatus newStatusEnum = OrderStatus.valueOf(newStatus.toUpperCase());
        if (order.getOrderStatus() == expectedStatusEnum) {
            order.setOrderStatus(newStatusEnum);
            orderRepository.save(order);
        } else {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }
}
