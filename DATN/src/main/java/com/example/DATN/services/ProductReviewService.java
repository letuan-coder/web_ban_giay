package com.example.DATN.services;

import com.example.DATN.constant.OrderStatus;
import com.example.DATN.dtos.request.ProductReviewRequest;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.models.Order;
import com.example.DATN.models.OrderItem;
import com.example.DATN.models.ProductReview;
import com.example.DATN.models.User;
import com.example.DATN.repositories.OrderItemRepository;
import com.example.DATN.repositories.OrderRepository;
import com.example.DATN.repositories.ProductReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductReviewService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductReviewRepository productReviewRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;

    @Transactional(rollbackOn = Exception.class)
    public void AddReview(
            String orderCode,
            ProductReviewRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getOrderStatus().equals(OrderStatus.COMPLETED)) {
            throw new ApplicationException((ErrorCode.UNCATEGORIZED_EXCEPTION));
        } else {
            OrderItem orderItem =orderItemRepository.findById(request.getOrderItemId())
                    .orElseThrow(()->new ApplicationException(ErrorCode.ORDER_ITEM_NOT_FOUND));
            orderItem.setRated(true);
            ProductReview productReview = ProductReview.builder()
                    .user(user)
                    .product(orderItem.getProductVariant().getProductColor().getProduct())
                    .order(order)
                    .orderItem(orderItem)
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .build();
            productReviewRepository.save(productReview);
        }
    }
}
