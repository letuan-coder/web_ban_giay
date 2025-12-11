package com.example.DATN.services;

import com.example.DATN.dtos.request.ProductReviewRequest;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.models.ProductReview;
import com.example.DATN.models.User;
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
    private final OrderRepository orderRepository;
    private final ProductReviewRepository productReviewRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;

    @Transactional(rollbackOn = Exception.class)
    public void AddReview(
//            String orderCode,
            ProductReviewRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();

//        Order order= orderRepository.findByOrderCode(orderCode)
//                .orElseThrow(()->new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
//        if(!order.getOrderStatus().equals(OrderStatus.COMPLETED)){
//            throw new ApplicationException((ErrorCode.UNCATEGORIZED_EXCEPTION));
//        }
//        else{
        ProductReview productReview = ProductReview.builder()
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        productReviewRepository.save(productReview);
    }
//    }

}
