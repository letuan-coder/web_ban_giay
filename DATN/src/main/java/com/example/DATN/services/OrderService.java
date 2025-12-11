package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.OrderStatus;
import com.example.DATN.constant.PaymentMethodEnum;
import com.example.DATN.constant.PaymentStatus;
import com.example.DATN.dtos.request.ghtk.GhnOrderInfo;
import com.example.DATN.dtos.request.ghtk.GhnProduct;
import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.order.OrderResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.OrderItemRepository;
import com.example.DATN.repositories.OrderRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.UserAddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ProductVariantRepository productVariantRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderItemRepository orderItemRepository;
    private final String OrderCodePrefix = "OD";

    @Transactional
    public void confirmOrder(Long orderId, OrderStatus status) {
        if (status == OrderStatus.CONFIRMED) {
            List<Integer> pickingShift = List.of(1, 2, 3);

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
            long codAmountLong = 0L;
            List<OrderItem> orderItem = orderItemRepository.findAllByOrder(order);
            if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
                codAmountLong = order.getTotal_price().longValue(); // ép BigDecimal sang long
            }
            List<GhnProduct> listProduct = new ArrayList<>();
            for (OrderItem item : orderItem) {
                Map<String, String> category = new HashMap<>();
                category.put("level1", item.getProductVariant().getProductColor().getProduct().getCategory().getName());
                GhnProduct product = GhnProduct.builder()
                        .name(item.getName())
                        .code(item.getCode())
                        .quantity(item.getQuantity())
                        .length(item.getLength())
                        .weight(item.getWeight())
                        .height(item.getHeight())
                        .width(item.getWidth())
                        .category(category)
                        .build();
                listProduct.add(product);
            }
            GhnOrderInfo ghnOrderInfo = GhnOrderInfo.builder()
//        Choose who pay shipping fee.
//        1: Shop/Seller.
//        2: Buyer/Consignee.
                    .payment_type_id(2)
                    .note(order.getNote())
                    .required_note("CHOXEMHANGKHONGTHU")//cho xem nhưng ko cho test
                    .from_name("đông tuấn store")
                    .from_phone("0777789337")
                    .from_address("672 huỳnh tấn phát ,P,Tân Phu,Q7")
                    .from_ward_name("Phường Tân Phú")
                    .from_district_name("Quận 7")
                    .from_province_name("HCM")
                    .return_address("180 cao lỗ")
                    .return_district_id(null)
                    .return_ward_code("")
                    .client_order_code("")
                    .to_name(order.getUserAddress().getReceiverName())
                    .to_phone(order.getUserAddress().getPhoneNumber())
                    .to_address(order.getUserAddress().getUserAddress())
                    .to_ward_code(order.getUserAddress().getWardCode())
                    .to_district_id(order.getUserAddress().getDistrictCode())
                    .cod_amount(codAmountLong)
                    .content("Giay dép sản phẩm ")
                    .weight(order.getTotal_weight())
                    .length(order.getTotal_length())
                    .width(order.getTotal_width())
                    .height(order.getTotal_height())
                    .pick_station_id(null)
                    .deliver_station_id(null)
                    .insurance_value(0L)
                    .service_id(0)
                    .service_type_id(order.getServiceId())
                    .coupon(null)
                    .pick_shift(pickingShift)
                    .items(listProduct)
                    .build();
            GhnService.createOrder(ghnOrderInfo);
        } else {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }

    }

    @Transactional(rollbackOn = Exception.class)
    public OrderResponse createOrder
            (OrderRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        UserAddress userAddress = userAddressRepository.findByUser(user)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND));
        Order order = new Order();
        order.setUser(user);
        Long code= snowflakeIdGenerator.nextId();
        String orderCode = OrderCodePrefix+code;
        order.setNote(request.getNote());
        order.setOrderCode(orderCode);
        order.setUserAddress(userAddress);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setPaymentMethod(PaymentMethodEnum.CASH_ON_DELIVERY);
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest orderItemRequest : request.getOrderItemRequests()) {

            Optional<ProductVariant> productVariantOpt =
                    productVariantRepository.findBysku(orderItemRequest.getSku());
            if(productVariantOpt.isPresent()) {
                ProductVariant item = productVariantOpt.get();
                OrderItem orderItem = new OrderItem();
                orderItem.setProductVariant(item);
                orderItem.setQuantity(orderItemRequest.getQuantity());
                orderItem.setName(item.getProductColor().getProduct().getName());
//                orderItem.setWeight(item.getWeight());
//                orderItem.setHeight(item.getHeight());
//                orderItem.setWidth(item.getWidth());
//                orderItem.setLength(item.getLength());
                orderItem.setWeight(request.getTotal_weight());
                orderItem.setHeight(request.getTotal_height());
                orderItem.setWidth(request.getTotal_width());
                orderItem.setLength(request.getTotal_length());
                orderItem.setCode(item.getSku());
                orderItem.setProductVariant(item);
                orderItem.setPrice(item.getPrice());
                orderItem.setCreatedAt(LocalDateTime.now());
                orderItem.setOrder(order);
                orderItem.setRated(false);
                orderItems.add(orderItem);
            }
        }
        order.setItems(orderItems);
        if(orderItems.isEmpty()){
            throw new ApplicationException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal
                                .valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer totalWeight = orderItems.stream()
                .mapToInt(i -> i.getWeight() * i.getQuantity())
                .sum();

        Integer totalLength = orderItems.stream()
                .mapToInt(i -> i.getLength() * i.getQuantity())
                .sum();

        Integer totalWidth = orderItems.stream()
                .mapToInt(i -> i.getWidth() * i.getQuantity())
                .sum();

        Integer totalHeight = orderItems.stream()
                .mapToInt(i -> i.getHeight() * i.getQuantity())
                .sum();
        order.setTotal_weight(totalWeight);
        order.setTotal_height(totalHeight);
        order.setTotal_width(totalWidth);
        order.setTotal_length(totalLength);
        order.setTotal_price(total);
        order.setServiceId(request.getServiceId());
        OrderResponse response = orderMapper.toResponse(orderRepository.save(order));
        response.setUserName(user.getUsername());
        return response;
    }

    public List<OrderResponse> getOrdersByUser() {
        User user = getUserByJwtHelper.getCurrentUser();
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream().map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId) {
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
                                  String newStatus) {
        Order order = findOrderById(orderId);
        OrderStatus newStatusEnum = OrderStatus.valueOf(newStatus.toUpperCase());
        if (order.getOrderStatus() != newStatusEnum) {
            order.setOrderStatus(newStatusEnum);
            orderRepository.save(order);
        } else {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }
}
