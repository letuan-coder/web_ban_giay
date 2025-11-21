package com.example.DATN.services;

import com.example.DATN.constant.OrderReturnStatus;
import com.example.DATN.constant.OrderStatus;
import com.example.DATN.dtos.request.OrderReturnRequest;
import com.example.DATN.dtos.request.RefundRequest;
import com.example.DATN.dtos.request.ReturnItemRequest;
import com.example.DATN.dtos.respone.OrderReturnResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderReturnMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderReturnService {

    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnItemRepository orderReturnItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final OrderReturnMapper orderReturnMapper;
    private final RefundService refundService;
    @Transactional
    public OrderReturnResponse createReturnRequest(OrderReturnRequest request) {
        User currentUser = getUserByJwtHelper.getCurrentUser();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredDate = order.getReceivedDate().plusDays(7);
        if(now.isAfter(expiredDate))
            throw new ApplicationException(ErrorCode.RETURN_PERIOD_EXPIRED);

        if (!Objects.equals(order.getUser().getId(), currentUser.getId())) {
            throw new ApplicationException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new ApplicationException(ErrorCode.ORDER_NOT_RETURNABLE);
        }
        boolean alreadyPending = order.getReturns().stream()
                .anyMatch(r -> r.getStatus() == OrderReturnStatus.PENDING);
        if (alreadyPending) {
            throw new ApplicationException(ErrorCode.RETURN_REQUEST_ALREADY_EXISTS);
        }
        OrderReturn orderReturn = new OrderReturn();
        orderReturn.setUser(currentUser);
        orderReturn.setOrder(order);
        orderReturn.setReasonReturn(request.getReason());
        orderReturn.setStatus(OrderReturnStatus.PENDING);

        RefundRequest refundRequest = RefundRequest.builder()
                .amount(order.getTotal_price())
                .reason(request.getReason())
                .order(order)
                .expectedRefundDate(now.plusDays(7))
                .build();
        refundService.createRefundRequest(refundRequest);
        List<OrderReturnItem> returnItems = new ArrayList<>();
        for (ReturnItemRequest itemRequest : request.getReturnItems()) {
            OrderItem orderItem = orderItemRepository.findById(itemRequest.getOrderItemId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_ITEM_NOT_FOUND));

            if (!Objects.equals(orderItem.getOrder().getId(), order.getId())) {
                throw new ApplicationException(ErrorCode.ACCESS_DENIED);
            }

            if (itemRequest.getQuantity() <= 0 || itemRequest.getQuantity() > orderItem.getQuantity()) {
                throw new ApplicationException(ErrorCode.RETURN_QUANTITY_INVALID);
            }

            OrderReturnItem returnItem = new OrderReturnItem();
            returnItem.setOrderReturn(orderReturn);
            returnItem.setOrderItem(orderItem);
            returnItem.setQuantity(itemRequest.getQuantity());
            returnItems.add(returnItem);
        }

        orderReturn.setReturnItems(returnItems);
        OrderReturnResponse response = orderReturnMapper.toOrderReturnResponse(orderReturnRepository.save(orderReturn));
        return response;
    }

    public Boolean checkReturnRequestExists(Long orderId) {
        User currentUser = getUserByJwtHelper.getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        if (!Objects.equals(order.getUser().getId(), currentUser.getId())) {
            throw new ApplicationException(ErrorCode.ACCESS_DENIED);
        }

        return order.getReturns().stream()
                .anyMatch(r -> r.getStatus() == OrderReturnStatus.PENDING);
    }

    @Transactional
    public OrderReturn approveReturnRequest(Long returnId) {
        OrderReturn orderReturn = orderReturnRepository.findById(returnId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_REQUEST_NOT_FOUND));

        if (orderReturn.getStatus() != OrderReturnStatus.PENDING) {
            throw new ApplicationException(ErrorCode.RETURN_STATUS_INVALID);
        }

        orderReturn.setStatus(OrderReturnStatus.APPROVED);
        return orderReturnRepository.save(orderReturn);
    }

    public OrderReturnResponse getReturnRequestId(Long returnId) {
        OrderReturn orderReturn = orderReturnRepository.findById((returnId))
                .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_REQUEST_NOT_FOUND));
        return orderReturnMapper.toOrderReturnResponse(orderReturn);
    }

    @Transactional
    public OrderReturn rejectReturnRequest(Long returnId) {
        // Add logic to check if user is admin

        OrderReturn orderReturn = orderReturnRepository.findById(returnId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_REQUEST_NOT_FOUND));

        if (orderReturn.getStatus() != OrderReturnStatus.PENDING) {
            throw new ApplicationException(ErrorCode.RETURN_STATUS_INVALID);
        }

        orderReturn.setStatus(OrderReturnStatus.REJECTED);
        return orderReturnRepository.save(orderReturn);
    }


    @Transactional
    public OrderReturn completeReturnRequest(Long returnId) {
        // Add logic to check if user is admin
        OrderReturn orderReturn = orderReturnRepository.findById(returnId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_REQUEST_NOT_FOUND));

        if (orderReturn.getStatus() != OrderReturnStatus.APPROVED) {
            throw new ApplicationException(ErrorCode.RETURN_STATUS_INVALID);
        }

        // Restore stock
        for (OrderReturnItem item : orderReturn.getReturnItems()) {
            ProductVariant variant = item.getOrderItem().getProductVariant();
            variant.setStock(variant.getStock() + item.getQuantity());
            productVariantRepository.save(variant);
        }

        orderReturn.setStatus(OrderReturnStatus.COMPLETED);
        return orderReturnRepository.save(orderReturn);
    }

    // Add methods to find/get return requests for users or admins
}
