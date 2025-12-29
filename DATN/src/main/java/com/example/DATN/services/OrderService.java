package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.*;
import com.example.DATN.dtos.request.ghtk.GhnOrderInfo;
import com.example.DATN.dtos.request.ghtk.GhnProduct;
import com.example.DATN.dtos.request.order.CheckOutRequest;
import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.respone.order.*;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductVariantMapper productVariantMapper;
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
    private final StockTransactionItemRepository stockTransactionItemRepository;
    private final StockRepository stockRepository;

    @Cacheable(value = "shipping_fee", key = "#address.wardCode")
    public Integer CalculateShippingFee(UserAddress userAddr, ProductVariant variant) {
        if (WeightTier.UP_TO_1KG.getMaxKg() == variant.getWeight() ){
            return 0 ;
        }
        return 1;
    }
//    public UUID FindStore (UserAddress userAddr){
//        Integer ward = Integer.parseInt(userAddr.getWardCode().trim());
//        List<Store> storeWards = storeRepository.findAllByWardCode(ward);
//        if (storeWards.isEmpty()) {
//            List<Store> storeDistrict = storeRepository.findAllByDistrictCode(userAddr.getDistrictCode());
//            if (storeDistrict.isEmpty()) {
//                Integer provinceId = Integer.parseInt(userAddr.getProvinceCode().trim());
//                List<Store> storeProvinces = storeRepository.findAllByProvinceCode(provinceId);
//
//            } else {
//                throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND, "chuyển qua kho tổng");
//            }
//        }
//        return storeWards.getLast().getId();
//    }
    public Integer calculateTotalStock (UUID storeId,ProductVariant variant){
        Integer quantity =0 ;
        List<Stock> stocks = stockRepository.findByVariant(variant);
        for(Stock stock : stocks){
           if (stock.getStockType() == StockType.STORE) {
               Stock storeStock = stockRepository.findByStoreId(storeId)
                       .orElseThrow(()-> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
               quantity += stock.getQuantity();
           }
        }
         return quantity;
    }
    public CheckOutResponse checkOutOrder(List<CheckOutRequest> request) {
        User user = getUserByJwtHelper.getCurrentUser();
        CheckOutResponse response = new CheckOutResponse();
        List<UserAddress> userAddress = userAddressRepository.findByUser(user);
        List<CheckOutProductResponse> checkOutProductResponses = new ArrayList<>();
        UserAddress userAddr = new UserAddress();
        for (UserAddress address : userAddress) {
            if (address.isDefault() == true) {
                userAddr = address;
            }
        }
        for (CheckOutRequest checkOutRequest : request) {
            ProductVariant productVariant = productVariantRepository.findById(checkOutRequest.getId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
            Integer ward = Integer.parseInt(userAddr.getWardCode());
            List<Store> storeWards = storeRepository.findAllByWardCode(ward);
            for(Store store : storeWards) {
                Integer stock = calculateTotalStock(store.getId(), productVariant);
            }
            Integer shippingFee = CalculateShippingFee(userAddr,productVariant);
            response.setExpectedFee(shippingFee);
            CheckOutProductResponse checkOutResponse = CheckOutProductResponse.builder()
                    .id(productVariant.getId())
                    .isAvailable(productVariant.getIsAvailable())
                    .quantity(checkOutRequest.getQuantity())
                    .sizeName(productVariant.getSize().getName())
                    .colorName(productVariant.getProductColor().getColor().getName())
                    .sku(productVariant.getSku())
                    .productName(productVariant.getProductColor().getProduct().getName())
                    .price(productVariant.getPrice())
                    .stock(100)
                    .imageUrl(productVariant.getProductColor().getImages().isEmpty() ? null :
                            productVariant.getProductColor().getImages().get(0).getImageUrl())
                    .build();

            checkOutProductResponses.add(checkOutResponse);
        }


        response.setUserAddressId(userAddr.getId());
        response.setProducts(checkOutProductResponses);
        return response;
    }

    @Transactional
    public void confirmOrder(UUID orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            List<Integer> pickingShift = List.of(1, 2, 3);
            long codAmountLong = 0L;
            List<OrderItem> orderItem = orderItemRepository.findAllByOrder(order);
            if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
                codAmountLong = order.getTotal_price().longValue();
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
                    .required_note("CHOXEMHANGKHONGTHU")
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

    public BigDecimal calculateTotalPrice(List<OrderItemRequest> orderItemRequests) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : orderItemRequests) {
            Optional<ProductVariant> productVariantOpt =
                    productVariantRepository.findBysku(itemRequest.getSku());
            if (productVariantOpt.isPresent()) {
                ProductVariant item = productVariantOpt.get();
                BigDecimal itemTotal = item.getPrice()
                        .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                totalPrice.add(itemTotal);
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
        if (request.getUserAddressesId() == null) {
            throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        UserAddress userAddress = userAddressRepository.findById(request.getUserAddressesId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND));
        Order order = new Order();
        order.setUser(user);
        Long code = snowflakeIdGenerator.nextId();
        String orderCode = OrderCodePrefix + code;
        order.setNote(request.getNote());
        order.setOrderCode(orderCode);
        order.setUserAddress(userAddress);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PROCESSCING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setPaymentMethod(PaymentMethodEnum.VNPAY);
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
        if (orderItems.isEmpty()) {
            throw new ApplicationException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal
                                .valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        Integer totalWeight = calculateTotalWeight(request.getOrderItemRequests());
        Integer totalWeight = request.getTotal_weight();
        Integer totalHeight = request.getTotal_height();
        Integer totalWidth = request.getTotal_width();
        Integer totalLength = request.getTotal_length();
//        Integer totalLength = calculateTotalLength(request.getOrderItemRequests());
//
//        Integer totalWidth = calculateTotalWidth(request.getOrderItemRequests());
//
//        Integer totalHeight = calculateTotalHeight(request.getOrderItemRequests());
        order.setTotal_weight(totalWeight);
        order.setTotal_height(totalHeight);
        order.setTotal_width(totalWidth);
        order.setTotal_length(totalLength);
        order.setTotal_price(total);
        VnPaymentRequest vnPaymentRequest = VnPaymentRequest.builder()
                .amount(total.longValue())
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
        pending.setUserAddressesId(order.getUserAddress());

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

    @Transactional(rollbackOn = Exception.class)
    public OrderResponse createOrder
            (OrderRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        if (request.getUserAddressesId() == null) {
            throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        UserAddress userAddress = userAddressRepository.findById(request.getUserAddressesId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND));
//        List<UserAddress> userAddress = userAddressRepository.findByUser(user);
//        UserAddress userAddr = new UserAddress();
//        for (UserAddress address : userAddress) {
//            if (address.isDefault() == true) {
//                userAddr = address;
//            }
//        }
        Order order = new Order();
        order.setUser(user);
        Long code = snowflakeIdGenerator.nextId();
        String orderCode = OrderCodePrefix + code;
        order.setNote(request.getNote());
        order.setOrderCode(orderCode);
        order.setUserAddress(userAddress);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
//        order.setOrderStatus(OrderStatus.COMPLETED);

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
        if (orderItems.isEmpty()) {
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
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setPaymentMethod(PaymentMethodEnum.CASH_ON_DELIVERY);
        order.setServiceId(request.getServiceId());
        OrderResponse response = orderMapper.toResponse(orderRepository.save(order));
        response.setUserName(user.getUsername());
        return response;
    }

    public List<OrderResponse> getOrdersByStatus(String status) {
        User user = getUserByJwtHelper.getCurrentUser();
        OrderStatus statusEnum;
        try {
            statusEnum = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }
        List<Order> orders = orderRepository.findAllByOrderStatusAndUser(statusEnum, user);
        return orders.stream().map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByUser() {
        User user = getUserByJwtHelper.getCurrentUser();

        List<Order> orders = orderRepository.findAllByUserId(user.getId());
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
}
