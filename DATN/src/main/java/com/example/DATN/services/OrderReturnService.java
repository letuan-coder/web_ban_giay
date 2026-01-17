package com.example.DATN.services;

import com.example.DATN.constant.OrderReturnStatus;
import com.example.DATN.constant.OrderStatus;
import com.example.DATN.dtos.request.OrderReturnRequest;
import com.example.DATN.dtos.request.RefundRequest;
import com.example.DATN.dtos.request.ReturnItemRequest;
import com.example.DATN.dtos.request.UploadImageRequest;
import com.example.DATN.dtos.respone.OrderReturnResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderReturnMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReturnService {
    private final ImageOrderReturnRepository imageOrderReturnRepository;

    private final OrderReturnRepository orderReturnRepository;
    private final MailService mailService;
    private final ImageProductService imageProductService;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final OrderReturnMapper orderReturnMapper;
    private final RefundService refundService;
    private final RedisTemplate<String, String> redisTemplate;
    private final OrderReturnItemRepository orderReturnItemRepository;

    public String acquireCancelLock(Long userId, UUID orderId) {
        String redisKey = "idempotency:cancel_order:" + userId + ":" + orderId;

        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofMinutes(5));

        if (Boolean.FALSE.equals(locked)) {
            throw new ApplicationException(ErrorCode.DUPLICATE_REQUEST);
        }
        return redisKey;
    }

    public boolean CheckingRequest(Long user) {
        String key = "cancel:count:" + user + ":" + LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
        boolean flag = false;
        if (count > 10) {
            flag = true;
        }
        return flag;
    }

    @Transactional(readOnly = true)
    public List<OrderReturnResponse> getAllOrderReturnRequest() {
        List<OrderReturn> orderReturn = orderReturnRepository.findAll();
        return orderReturn.stream().map(orderReturnMapper::toOrderReturnResponse).toList();
    }

    @Transactional
    public OrderReturnResponse createReturnRequest
            (OrderReturnRequest request, Map<String, List<MultipartFile>> files) {
        User currentUser = getUserByJwtHelper.getCurrentUser();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        if (CheckingRequest(currentUser.getId())) {
            throw new ApplicationException(ErrorCode.TOO_MANY_CANCEL_REQUEST);
        }
        if (files != null) {
            files.forEach((k, v) -> {
                log.info("FILES KEY = {}, COUNT = {}", k, v.size());
                v.forEach(f -> log.info(
                        " - filename={}, size={}",
                        f.getOriginalFilename(),
                        f.getSize()
                ));
            });
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredDate = order.getReceivedDate().plusDays(7);
        if (now.isAfter(expiredDate)) {
            throw new ApplicationException(ErrorCode.RETURN_PERIOD_EXPIRED);

        }
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
        orderReturn.setReasonReturn(request.getReason());

        orderReturn.setOrder(order);
        orderReturn.setReturnType(request.getReturnType());
        orderReturn.setReasonReturn(request.getReason());
        orderReturn.setStatus(OrderReturnStatus.PENDING);
        orderReturnRepository.save(orderReturn);
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
            String key = itemRequest.getOrderItemId().toString();

            List<MultipartFile> images =
                    files != null
                            ? files.getOrDefault(key, List.of())
                            : List.of();
            if (images.size() > 5) {
                throw new ApplicationException(ErrorCode.FILE_COUNT_EXCEEDED);
            }
            OrderReturnItem returnItem = new OrderReturnItem();
            returnItem.setOrderReturn(orderReturn);
            returnItem.setOrderItem(orderItem);
            returnItem.setQuantity(itemRequest.getQuantity());
            returnItems.add(returnItem);
            OrderReturnItem item = orderReturnItemRepository.save(returnItem);
            List<ImageOrderReturn> imageOrderReturns = new ArrayList<>();
            for (MultipartFile file : images) {

                ImageOrderReturn imageOrderReturn = ImageOrderReturn.builder()
                        .orderReturnItem(item)
                        .build();
                UploadImageRequest uploadImageRequest = UploadImageRequest.builder()
                        .file(file)
                        .imageOrderReturn(imageOrderReturn)
                        .build();
                imageProductService.uploadImage(uploadImageRequest);
                imageOrderReturns.add(imageOrderReturn);
            }
            item.setImages(imageOrderReturns);
            orderReturnItemRepository.save(item);
            imageOrderReturnRepository.saveAll(imageOrderReturns);
        }

        orderReturn.setReturnItems(returnItems);
        return orderReturnMapper.toOrderReturnResponse(orderReturnRepository.save(orderReturn));
    }

    public void ExchangeItem() {

    }

    public Boolean checkReturnRequestExists(UUID orderId) {
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
    public OrderReturn approveReturnRequest(UUID returnId) {

        OrderReturn orderReturn = orderReturnRepository.findById(returnId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_REQUEST_NOT_FOUND));

        if (orderReturn.getStatus() != OrderReturnStatus.PENDING) {
            throw new ApplicationException(ErrorCode.RETURN_STATUS_INVALID);
        }
        orderReturn.setStatus(OrderReturnStatus.APPROVED);
        return orderReturnRepository.save(orderReturn);
    }

    public OrderReturnResponse getReturnRequestId(UUID returnId) {
        OrderReturn orderReturn = orderReturnRepository.findById((returnId))
                .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_REQUEST_NOT_FOUND));
        return orderReturnMapper.toOrderReturnResponse(orderReturn);
    }

    @Transactional
    public OrderReturn rejectReturnRequest(UUID returnId) {
        // Add logic to check if user is admin

        OrderReturn orderReturn = orderReturnRepository.findById(returnId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_REQUEST_NOT_FOUND));

        if (orderReturn.getStatus() != OrderReturnStatus.PENDING) {
            throw new ApplicationException(ErrorCode.RETURN_STATUS_INVALID);
        }

        orderReturn.setStatus(OrderReturnStatus.REJECTED);
        return orderReturnRepository.save(orderReturn);
    }


//    @Transactional
//    public OrderReturn completeReturnRequest(Long returnId) {
//        // Add logic to check if user is admin
//        OrderReturn orderReturn = orderReturnRepository.findById(returnId)
//                .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_REQUEST_NOT_FOUND));
//
//        if (orderReturn.getStatus() != OrderReturnStatus.APPROVED) {
//            throw new ApplicationException(ErrorCode.RETURN_STATUS_INVALID);
//        }
//
//        // Restore stock
//        for (OrderReturnItem item : orderReturn.getReturnItems()) {
//            ProductVariant variant = item.getOrderItem().getProductVariant();
//            variant.setStocks(variant. + item.getQuantity());
//            productVariantRepository.save(variant);
//        }
//
//        orderReturn.setStatus(OrderReturnStatus.COMPLETED);
//        return orderReturnRepository.save(orderReturn);
//    }

    // Add methods to find/get return requests for users or admins
}
