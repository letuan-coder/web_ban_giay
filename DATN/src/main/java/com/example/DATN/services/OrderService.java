package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.OrderStatus;
import com.example.DATN.constant.PaymentMethodEnum;
import com.example.DATN.constant.PaymentStatus;
import com.example.DATN.constant.ShippingStatus;
import com.example.DATN.dtos.request.ghtk.GhnOrderInfo;
import com.example.DATN.dtos.request.ghtk.GhnProduct;
import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.request.vnpay.VnPayRefundRequest;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.respone.order.*;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.models.Embeddable.ShippingAddress;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final VnpayRepository vnpayRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ProductVariantRepository productVariantRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderItemRepository orderItemRepository;
    private final String OrderCodePrefix = "OD";
    private final VnPayServices vnPayServices;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final StoreRepository storeRepository;
    private final StockRepository stockRepository;
    private static final String IDEMPOTENCY_PREFIX = "checkout:idempotency:";
    private CheckOutResponse getIdempotentResponse(String key) {
        try {
            String cacheKey = IDEMPOTENCY_PREFIX + key;
            String cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                return objectMapper.readValue(cached, CheckOutResponse.class);
            }
        } catch (Exception e) {
            log.warn("Failed to get idempotent response for key: {}", key, e);
        }
        return null;
    }


    public void cancelOrder(UUID orderId, HttpServletRequest req) {
        User user = getUserByJwtHelper.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            Vnpay vnpay = vnpayRepository.findByVnpTxnRef(order.getOrderCode())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_VNPAY_FAIL));
            VnPayRefundRequest refundRequest = VnPayRefundRequest.builder()
                    .txnRef(order.getOrderCode())
                    .orderInfo(vnpay.getVnp_OrderInfo())
                    .CreateBy(user.getUsername())
                    .transactionDate(order.getCreatedAt().toString())
                    .amount(Long.parseLong(vnpay.getVnp_Amount()))
                    .transactionType("02")
                    .build();
            vnPayServices.processRefund(refundRequest, req);

        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new ApplicationException(ErrorCode.ORDER_NOT_CANCEABLE);
        } else {
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }


    private Store findStoreHasStock(
            ProductVariant variant,
            Integer quantity,
            List<Store> stores
    ) {
        for (Store store : stores) {
            Optional<Stock> stockOpt =
                    stockRepository.findByVariant_IdAndStore(variant.getId(), store);

            if (stockOpt.isPresent()
                    && stockOpt.get().getQuantity() >= quantity) {
                return store;
            }
        }
        return null;
    }

    public Store FindStore(ProductVariant variant, Integer quantity, UserAddress addr) {
        Integer wardCode = Integer.parseInt(addr.getWardCode().trim());

        List<Store> stores = storeRepository.findAllByWardCode(wardCode);
        Store store = findStoreHasStock(variant, quantity, stores);
        if (store != null) return store;

        stores = storeRepository.findAllByDistrictCode(addr.getDistrictCode());
        store = findStoreHasStock(variant, quantity, stores);
        if (store != null) return store;

        Integer provinceCode = Integer.parseInt(addr.getProvinceCode().trim());
        stores = storeRepository.findAllByProvinceCode(provinceCode);
        return findStoreHasStock(variant, quantity, stores);
    }


    @Transactional
    public void confirmOrder(UUID orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            List<Integer> pickingShift = List.of(1, 2, 3);
            long codAmountLong = 0L;
            List<OrderItem> orderItem = orderItemRepository.findAllByOrder_Id(orderId);
            if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
                codAmountLong = order.getTotal_price().longValue();
            }
            List<GhnProduct> listProduct = new ArrayList<>();
            for (OrderItem item : orderItem) {
                Map<String, String> category = new HashMap<>();
                category.put("level1", item.getProductVariant()
                        .getProductColor().getProduct().getCategory().getName());
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
                    .required_note("CHOXEMHANGKHONGTHU")
                    .from_name("đông tuấn store")
                    .from_phone("0777789337")
                    .from_address("672 huỳnh tấn phát ,P,Tân Phu,Q7")
                    .from_ward_name("Phường Tân Phú")
                    .from_district_name("Quận 7")
                    .from_province_name("HCM")
                    .return_address("180 cao lỗ")
                    .return_district_id(null)
                    .return_ward_code("13010")
                    .client_order_code("")
                    .to_name(order.getUserAddresses().getDistrictName())
                    .to_phone(order.getUserAddresses().getPhoneNumber())
                    .to_address(order.getUserAddresses().getFullDetail())
                    .to_ward_code(order.getUserAddresses().getWardCode())
                    .to_district_id(order.getUserAddresses().getDistrict_Id())
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
                    .service_type_id(2)
                    .coupon(null)
                    .pick_shift(pickingShift)
                    .items(listProduct)
                    .build();
            GhnService.createOrder(ghnOrderInfo);
        } else {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }

    }

    public BigDecimal calculateTotalPrice(List<OrderItemRequest> orderItemRequests) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : orderItemRequests) {
            Optional<ProductVariant> productVariantOpt =
                    productVariantRepository.findBysku(itemRequest.getSku());
            if (productVariantOpt.isPresent()) {
                ProductVariant item = productVariantOpt.get();
                BigDecimal itemTotal = item.getPrice()
                        .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                totalPrice =totalPrice.add(itemTotal);
            }
        }
        return totalPrice;
    }

    public Integer calculateTotalWeight(List<OrderItemRequest> orderItemRequests) {
        Integer totalWeight = 0;
        for (OrderItemRequest itemRequest : orderItemRequests) {
            Optional<ProductVariant> productVariantOpt =
                    productVariantRepository.findBysku(itemRequest.getSku());
            if (productVariantOpt.isPresent()) {
                ProductVariant item = productVariantOpt.get();
                totalWeight += item.getWeight() * itemRequest.getQuantity();
            }
        }
        return totalWeight;
    }

    public Integer calculateTotalHeight(List<OrderItemRequest> orderItemRequests) {
        Integer totalHeight = 0;
        for (OrderItemRequest itemRequest : orderItemRequests) {
            Optional<ProductVariant> productVariantOpt =
                    productVariantRepository.findBysku(itemRequest.getSku());
            if (productVariantOpt.isPresent()) {
                ProductVariant item = productVariantOpt.get();
                totalHeight += item.getHeight() * itemRequest.getQuantity();
            }
        }
        return totalHeight;
    }

    public Integer calculateTotalLength(List<OrderItemRequest> orderItemRequests) {
        Integer totalLength = 0;
        for (OrderItemRequest itemRequest : orderItemRequests) {
            Optional<ProductVariant> productVariantOpt =
                    productVariantRepository.findBysku(itemRequest.getSku());
            if (productVariantOpt.isPresent()) {
                ProductVariant item = productVariantOpt.get();
                totalLength += item.getLength() * itemRequest.getQuantity();
            }
        }
        return totalLength;
    }

    public Integer calculateTotalWidth(List<OrderItemRequest> orderItemRequests) {
        Integer totalWidth = 0;
        for (OrderItemRequest itemRequest : orderItemRequests) {
            Optional<ProductVariant> productVariantOpt =
                    productVariantRepository.findBysku(itemRequest.getSku());
            if (productVariantOpt.isPresent()) {
                ProductVariant item = productVariantOpt.get();
                totalWidth += item.getWidth() * itemRequest.getQuantity();
            }
        }
        return totalWidth;
    }

    @Transactional(rollbackOn = Exception.class)
    public OrderResponse createOrderVnPay
            (OrderRequest request, HttpServletRequest Serverletrequest)
            throws JsonProcessingException {
        User user = getUserByJwtHelper.getCurrentUser();
        ShippingAddress shippingAddress = createShippingAddress(request);
        Order order = BuilderOrder(request,user,shippingAddress);
        VnPaymentRequest vnPaymentRequest = VnPaymentRequest.builder()
                .amount(order.getTotal_price().longValue())
                .orderCode(order.getOrderCode())
                .bankCode(request.getBankCode())
                .build();
        order.setServiceId(request.getServiceId());
        OrderResponse response = orderMapper.toResponse(order);
        response.setUserName(user.getUsername());
        createPendingOrderInRedis(order);
        String url = vnPayServices.createPaymentVNPAY(vnPaymentRequest, Serverletrequest);
        response.setResponse(url);
        return response;
    }

    public PendingOrderRedis mapFromOrder(Order order) {
        PendingOrderRedis pending = new PendingOrderRedis();
        pending.setOrderCode(order.getOrderCode());
        pending.setNote(order.getNote());
        ShippingAddressRedis redis = orderMapper.toAddress(order.getUserAddresses());
        pending.setUserAddresses(redis);
        pending.setUserId(order.getUser().getId());
        List<PendingOrderItem> pendingItems = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            PendingOrderItem pendingItem = new PendingOrderItem();
            pendingItem.setID(item.getId());
            pendingItem.setName(item.getName());
            pendingItem.setSku(item.getProductVariant().getSku());
            pendingItem.setQuantity(item.getQuantity());
            pendingItem.setPrice(item.getPrice());
            pendingItem.setHeight(item.getHeight());
            pendingItem.setWeight(item.getWeight());
            pendingItem.setLength(item.getLength());
            pendingItem.setWidth(item.getWidth());
            pendingItems.add(pendingItem);
        }
        pending.setItems(pendingItems);
        pending.setTotalPrice(order.getTotal_price());
        pending.setTotalWeight(order.getTotal_weight());
        pending.setTotalHeight(order.getTotal_height());
        pending.setTotalWidth(order.getTotal_width());
        pending.setTotalLength(order.getTotal_length());
        return pending;
    }

    public void createPendingOrderInRedis(Order order) throws JsonProcessingException {
        PendingOrderRedis pending = mapFromOrder(order);
        redisTemplate.opsForValue().set(
                "ORDER_PENDING:" + order.getOrderCode(),
                objectMapper.writeValueAsString(pending),
                15,
                TimeUnit.MINUTES
        );

    }

    public ShippingAddress createShippingAddress(OrderRequest request){
        ShippingAddress shippingAddress = new ShippingAddress();
        if(request.getUserAddressId()!=null) {
            UserAddress userAddress = userAddressRepository.findById(request.getUserAddressId())
                    .orElseThrow(()->new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND));
            shippingAddress = ShippingAddress.builder()
                    .receiverName(userAddress.getReceiverName())
                    .phoneNumber(userAddress.getPhoneNumber())
                    .provinceName(userAddress.getProvinceName())
                    .districtName(userAddress.getDistrictName())
                    .wardName(userAddress.getWardName())
                    .district_Id(userAddress.getDistrictCode())
                    .wardCode(userAddress.getWardCode())
                    .streetDetail(userAddress.getStreetDetail())
                    .fullDetail(userAddress.getUserAddress())
                    .build();
        }else if(request.getUserAddress() != null){
            shippingAddress = ShippingAddress.builder()
                    .receiverName(request.getUserAddress().getReceiverName())
                    .phoneNumber(request.getUserAddress().getPhoneNumber())
                    .provinceName(request.getUserAddress().getProvinceName())
                    .districtName(request.getUserAddress().getDistrictName())
                    .district_Id(request.getUserAddress().getDistrict_Id())
                    .wardName(request.getUserAddress().getWardName())
                    .wardCode(request.getUserAddress().getWardCode())
                    .streetDetail(request.getUserAddress().getStreetDetail())
                    .fullDetail(request.getUserAddress().getFullDetail())
                    .build();

        }else {
            throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        return shippingAddress;
    }

    @Transactional(rollbackOn = Exception.class)
    public OrderResponse createOrder
            (OrderRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        ShippingAddress shippingAddress =createShippingAddress(request);
        Order order = BuilderOrder(request,user,shippingAddress);
        OrderResponse response = orderMapper.toResponse(orderRepository.save(order));
        response.setUserName(user.getUsername());
        return response;
    }

    public String GenerateOrderCode(){
        Long code = snowflakeIdGenerator.nextId();
        String orderCode = OrderCodePrefix + code;
        return orderCode;
    }
    @Transactional(rollbackOn = Exception.class)
    public Order BuilderOrder(OrderRequest request,User user,ShippingAddress shippingAddress){
        Order order = new Order();
        order.setUser(user);
        String orderCode = GenerateOrderCode();
        order.setNote(request.getNote());
        order.setOrderCode(orderCode);
        order.setUserAddresses(shippingAddress);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest orderItemRequest : request.getOrderItemRequests()) {

            Optional<ProductVariant> productVariantOpt =
                    productVariantRepository.findBysku(orderItemRequest.getSku());
            if (productVariantOpt.isPresent()) {
                ProductVariant item = productVariantOpt.get();
                OrderItem orderItem = new OrderItem();
                orderItem.setProductVariant(item);
                orderItem.setQuantity(orderItemRequest.getQuantity());
                orderItem.setName(item.getProductColor().getProduct().getName());
                orderItem.setWeight(item.getWeight());
                orderItem.setHeight(item.getHeight());
                orderItem.setWidth(item.getWidth());
                orderItem.setLength(item.getLength());
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
        if (orderItems.isEmpty()) {
            throw new ApplicationException(ErrorCode.ORDER_ITEM_NOT_FOUND);
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
        order.setShippingFee(request.getShippingFee());
        order.setTotal_price(total.add(request.getShippingFee()));
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setPaymentMethod(PaymentMethodEnum.CASH_ON_DELIVERY);
        order.setServiceId(request.getServiceId());
        return order;
    }
    public List<OrderResponse> getOrdersByStatus(String status) {
        User user = getUserByJwtHelper.getCurrentUser();
        OrderStatus statusEnum;
        try {
            statusEnum = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }
        List<Order> orders = orderRepository.findAllByOrderStatusAndUserOrderByCreatedAtDesc(statusEnum, user);
        return orders.stream().map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByUser() {
        User user = getUserByJwtHelper.getCurrentUser();

        List<Order> orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        return orders.stream().map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersForAdmin() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orders.stream().map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }


    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toResponse(order);
    }

    public Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    public void updateOrderStatus(UUID orderId,
                                  String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
        OrderStatus newStatusEnum = OrderStatus.valueOf(newStatus.toUpperCase());
        if (order.getOrderStatus() != newStatusEnum) {
            order.setOrderStatus(newStatusEnum);
            orderRepository.save(order);
        } else {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }

    @Transactional
    public void updateShippingStatus(UUID orderId, String newStatus) {
        Order order = findOrderById(orderId);
        ShippingStatus newStatusEnum = ShippingStatus.valueOf(newStatus.toUpperCase());
        if (order.getGhnStatus() != newStatusEnum) {
            order.setGhnStatus(newStatusEnum);
            orderRepository.save(order);
        } else {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }
}
