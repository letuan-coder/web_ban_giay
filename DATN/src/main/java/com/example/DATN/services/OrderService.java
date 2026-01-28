package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.*;
import com.example.DATN.dtos.request.ghtk.GhnOrderInfo;
import com.example.DATN.dtos.request.ghtk.GhnProduct;
import com.example.DATN.dtos.request.order.CancelOrderRequest;
import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.request.vnpay.VnPayRefundRequest;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.respone.ghn.GhnOrderSyncResponse;
import com.example.DATN.dtos.respone.ghn.GhnStatusLogDto;
import com.example.DATN.dtos.respone.order.CancelOrderResponse;
import com.example.DATN.dtos.respone.order.CheckOutProductResponse;
import com.example.DATN.dtos.respone.order.CheckOutResponse;
import com.example.DATN.dtos.respone.order.OrderResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.models.Embeddable.ShippingAddress;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final GhnOrderStatusLogRepository ghnOrderStatusLogRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final GhnService ghnService;
    private final VoucherRepository voucherRepository;
    private final StockReservationRepository stockReservationRepository;
    private final VnpayRepository vnpayRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ProductVariantRepository productVariantRepository;
    private final OrderItemRepository orderItemRepository;
    private final String OrderCodePrefix = "OD";
    private final VnPayServices vnPayServices;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final StoreRepository storeRepository;
    private final StockRepository stockRepository;
    private LoadingCache<String, ProductVariant> variantCache;
    private static final String IDEMPOTENCY_PREFIX = "checkout:idempotency:";

    @PostConstruct
    public void init() {
        variantCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build(sku -> productVariantRepository.findBysku(sku)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND)));
    }

    private CheckOutResponse getIdempotentResponse(String key) {
        try {
            String cacheKey = IDEMPOTENCY_PREFIX + key;
            String cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                return objectMapper.readValue(
                        cached,
                        CheckOutResponse.class
                );
//                return objectMapper.readValue(cached, CheckOutResponse.class);
            }
        } catch (Exception e) {
            log.warn("Failed to get idempotent response for key: {}", key, e);
        }
        return null;
    }

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


    @Transactional(rollbackOn = Exception.class)
    public CancelOrderResponse cancelOrder(CancelOrderRequest request, HttpServletRequest req) {
        User user = getUserByJwtHelper.getCurrentUser();
        if (CheckingRequest(user.getId())) {
            throw new ApplicationException(ErrorCode.TOO_MANY_CANCEL_REQUEST);
        }
        String redisKey = acquireCancelLock(user.getId(), request.getOrderId());
        try {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));
            if (order.getOrderStatus() != OrderStatus.PENDING) {
                throw new ApplicationException(ErrorCode.ORDER_NOT_CANCEABLE);
            }
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                Vnpay vnpay = vnpayRepository.findByVnpTxnRef(order.getOrderCode())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_VNPAY_FAIL));
                VnPayRefundRequest refundRequest = VnPayRefundRequest.builder()
                        .txnRef(order.getOrderCode())
                        .TransactionNo(vnpay.getVnp_TransactionNo())
                        .orderInfo(vnpay.getVnp_OrderInfo())
                        .CreateBy(user.getUsername())
                        .transactionDate(order.getCreatedAt().toString())
                        .amount(Long.parseLong(vnpay.getVnp_Amount()))
                        .transactionType("02")
                        .build();
                CancelOrderResponse response =
                        vnPayServices.processRefund(refundRequest, req, order, request.getReason());
                return response;
            }
        } catch (Exception e) {
            redisTemplate.delete(redisKey);
            throw new ApplicationException(ErrorCode.ORDER_NOT_FOUND);
        }
        return null;
    }


    @Transactional
    public void confirmOrder(UUID orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            Store store =order.getStoreDelivered();
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
                        .price(item.getPrice().longValue())
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
                    .payment_type_id(1)
                    .note(order.getNote())
                    .required_note("CHOXEMHANGKHONGTHU")
                    .from_name(store.getName())
                    .from_phone(store.getPhoneNumber())
                    .from_address(store.getLocation())
                    .from_ward_name(store.getWardName())
                    .from_district_name(store.getDistrictName())
                    .from_province_name(store.getProvinceName())
                    .return_address(store.getLocation())
                    .return_district_id(null)
                    .return_ward_code(store.getWardCode())
                    .client_order_code(order.getOrderCode())
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
            ghnService.createOrder(ghnOrderInfo,order);
        } else {
            throw new ApplicationException(ErrorCode.ORDER_STATUS_INVALID);
        }

    }


    @Transactional(rollbackOn = Exception.class)
    public OrderResponse createOrderVnPay
            (OrderRequest request, HttpServletRequest Serverletrequest)
    {
        String idempotencyKey = request.getIdempotency();
        CheckOutResponse cacheResponse = getIdempotentResponse(idempotencyKey);
        if (cacheResponse != null) {
            String orderCode = GenerateOrderCode();
            for (CheckOutProductResponse productResponse : cacheResponse.getProducts()) {
                int updated = stockRepository.lockStock(productResponse.getStockResponse().getId(),
                        productResponse.getQuantity());
                if (updated == 0) {
                    throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
                }
                StockReservation reservation = StockReservation.builder()
                        .orderCode(orderCode)
                        .status(StockReservationStatus.HOLD)
                        .stockId(productResponse.getStockResponse().getId())
                        .qty(productResponse.getQuantity())
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build();
                stockReservationRepository.save(reservation);

            }
            User user = getUserByJwtHelper.getCurrentUser();
            Order order = BuilderOrder(cacheResponse, user);

            order.setNote(request.getNote());
            order.setOrderCode(orderCode);
            VnPaymentRequest vnPaymentRequest = VnPaymentRequest.builder()
                    .amount(order.getFinalPrice().longValue())
                    .orderCode(orderCode)
                    .bankCode(request.getBankCode())
                    .build();
            order.setServiceId(request.getServiceId());
            OrderResponse response = orderMapper.toResponse(orderRepository.save(order));
            if (cacheResponse.getVoucherId()!=null) {
                Voucher voucher = voucherRepository.findById(cacheResponse.getVoucherId())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
                voucher.setUsedCount(voucher.getUsedCount()+1);
                voucherRepository.save(voucher);
                VoucherUsage usage = VoucherUsage.builder()
                        .order(order)
                        .user(user)
                        .voucherType(voucher.getType())
                        .voucher(voucher)
                        .discountAmount(cacheResponse.getVoucherDiscount())
                        .usedAt(LocalDateTime.now())
                        .build();
                voucherUsageRepository.save(usage);
            }
            orderItemRepository.saveAll(order.getItems());
//            response.setUserName(user.getUsername());
            String url = vnPayServices.createPaymentVNPAY(vnPaymentRequest, Serverletrequest);
            response.setResponse(url);
            return response;
        } else {
            throw new ApplicationException(ErrorCode.IDEMPOTENCY_TIMEOUT);
        }
    }

    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void releaseExpiredStockReservations() {
        List<StockReservation> expired =
                stockReservationRepository.findExpiredReservations();
        for (StockReservation reservation : expired) {
            int mark = stockReservationRepository.markExpiredReleased(reservation.getId());
            UUID stockId = reservation.getStockId();
            int qty = reservation.getQty();
            int updated = stockRepository.unlockStock(stockId, qty);
            if (updated > 0) {
                reservation.setStatus(StockReservationStatus.RELEASE);
            }
            stockReservationRepository.save(reservation);
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public OrderResponse createOrder
            (OrderRequest request) {
        CheckOutResponse cacheResponse = getIdempotentResponse(request.getIdempotency());
        if (cacheResponse != null) {
            String orderCode = GenerateOrderCode();
            for (CheckOutProductResponse productResponse : cacheResponse.getProducts()) {
                int updated = stockRepository.lockStock(productResponse.getStockResponse().getId(),
                        productResponse.getQuantity());
                if (updated == 0) {
                    throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
                }
            }
            User user = getUserByJwtHelper.getCurrentUser();
            Order order = BuilderOrder(cacheResponse, user);
            order.setOrderCode(orderCode);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setNote(request.getNote());
            OrderResponse response = orderMapper.toResponse(orderRepository.save(order));
            if (cacheResponse.getVoucherId()!=null) {
                Voucher voucher = voucherRepository.findById(cacheResponse.getVoucherId())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
                voucher.setUsedCount(voucher.getUsedCount() + 1);
                voucherRepository.save(voucher);
                VoucherUsage usage = VoucherUsage.builder()
                        .order(order)
                        .user(user)
                        .voucherType(voucher.getType())
                        .voucher(voucher)
                        .discountAmount(cacheResponse.getVoucherDiscount())
                        .usedAt(LocalDateTime.now())
                        .build();
                voucherUsageRepository.save(usage);
            }
            response.setUserName(user.getUsername());
            for (CheckOutProductResponse productResponse : cacheResponse.getProducts()) {
                stockRepository.commitStock(
                        productResponse.getStockResponse().getId(),
                        productResponse.getQuantity()
                );
            }
            String cachekey = IDEMPOTENCY_PREFIX + request.getIdempotency();
            redisTemplate.delete(cachekey);
            return response;
        } else {
            throw new ApplicationException(ErrorCode.IDEMPOTENCY_TIMEOUT);
        }
    }


    @Transactional
    public Order BuilderOrder(CheckOutResponse response, User user) {
        ShippingAddress shippingAddress = null;
        if (response.getShippingAddressResponse() != null) {
            shippingAddress = orderMapper.
                    toShipping(response.getShippingAddressResponse());
        } else {
            throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        UUID storeId = response.getStoreId();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        Order order = Order.builder()
                .user(user)
                .storeDelivered(store)
                .userAddresses(shippingAddress)
                .orderStatus(OrderStatus.PROCESSCING)
                .build();
        List<OrderItem> orderItems = new ArrayList<>();

        for (CheckOutProductResponse orderItemRequest : response.getProducts()) {
            ProductVariant item = getVariantCache(orderItemRequest.getSku());
            Stock stock = stockRepository.findById(orderItemRequest.getStockResponse().getId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
            OrderItem orderItem = new OrderItem();
            orderItem.setProductVariant(item);
            orderItem.setStock(stock);
            orderItem.setQuantity(orderItemRequest.getQuantity());
            orderItem.setName(orderItemRequest.getProductName());
            orderItem.setWeight(item.getWeight());
            orderItem.setHeight(item.getHeight());
            orderItem.setWidth(item.getWidth());
            orderItem.setLength(item.getLength());
            orderItem.setWeight(orderItem.getWeight());
            orderItem.setHeight(orderItem.getHeight());
            orderItem.setWidth(orderItem.getWidth());
            orderItem.setLength(orderItem.getLength());
            orderItem.setCode(item.getSku());
            orderItem.setProductVariant(item);
            orderItem.setPrice(item.getPrice());
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setOrder(order);
            orderItem.setRated(false);
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
        if (orderItems.isEmpty()) {
            throw new ApplicationException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        order.setTotal_weight(response.getTotal_weight());
        order.setTotal_height(response.getTotal_height());
        order.setTotal_width(response.getTotal_width());
        order.setTotal_length(response.getTotal_length());
        order.setShippingFee(response.getShippingFee());
        order.setTotal_price(response.getFinalPrice());
        order.setFinalPrice(response.getFinalPrice().add(order.getShippingFee()));
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setPaymentMethod(PaymentMethodEnum.CASH_ON_DELIVERY);

        return order;
    }

    public ProductVariant getVariantCache(String sku) {
        return variantCache.get(sku);
    }

    public String GenerateOrderCode() {
        Long code = snowflakeIdGenerator.nextId();
        String orderCode = OrderCodePrefix + code;
        return orderCode;
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
    private ShippingStatus mapGhnStatus(String status) {
        return switch (status) {
            case "picking"    -> ShippingStatus.PICKING;
            case "picked"     -> ShippingStatus.PICKED;
            case "storing"    -> ShippingStatus.STORING;
            case "delivering" -> ShippingStatus.DELIVERING;
            case "delivered"  -> ShippingStatus.DELIVERED;
            case "return"     -> ShippingStatus.RETURNED;
            case "cancel"     -> ShippingStatus.CANCEL;
            default -> ShippingStatus.DAMAGE;
        };
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void syncGhnOrderStatus() {
        LocalDateTime syncBefore = LocalDateTime.now().minusMinutes(5);
        List<Order> orders =
                orderRepository.findOrdersNeedSync(syncBefore);

        for (Order order : orders) {
            try {
                GhnOrderSyncResponse response =
                        ghnService.getOrderDetail(
                                order.getGhn().getGhnOrderCode()
                        );

                String ghnStatus = response.getData().getStatus();
                List<GhnStatusLogDto> logs = response.getData().getLog();

                ShippingStatus newStatus = mapGhnStatus(ghnStatus);

                LocalDateTime lastUpdateTime =
                        logs.stream()
                                .map(l -> l.getUpdatedDate())
                                .max(LocalDateTime::compareTo)
                                .orElse(
                                        response.getData()
                                                .getUpdatedDate()
                                                .toLocalDateTime()
                                );
                updateOrderFromGhn(order, newStatus, lastUpdateTime);
                saveGhnLogs(order, logs);
                order.resetFailCount();
            } catch (Exception e) {
                order.increaseFailCount();
            }
            order.updateLastSyncAt();
            orderRepository.save(order);
        }
    }
    private void updateOrderFromGhn(
            Order order,
            ShippingStatus newStatus,
            LocalDateTime lastUpdateTime
    ) {
        ShippingStatus oldStatus = order.getGhnStatus();

        if (newStatus != null && oldStatus != newStatus) {
            order.setGhnStatus(newStatus);

            if (newStatus == ShippingStatus.DELIVERED) {
                order.setReceivedDate(lastUpdateTime);
                order.setOrderStatus(OrderStatus.COMPLETED);
            }

            if (newStatus == ShippingStatus.CANCEL
                    || newStatus == ShippingStatus.RETURNED) {
                order.setOrderStatus(OrderStatus.CANCELLED);
            }
        }

        order.getGhn().setGhnLastUpdated(lastUpdateTime);
    }

    private void saveGhnLogs(
            Order order,
            List<GhnStatusLogDto> logs
    ) throws JsonProcessingException {
        for (GhnStatusLogDto log : logs) {

            boolean exists =
                    ghnOrderStatusLogRepository.existsByOrder_IdAndStatusAndGhnUpdatedAt(
                            order.getId(),
                            log.getStatus(),
                            log.getUpdatedDate());

            if (!exists) {
                GhnOrderStatusLog entity =
                        GhnOrderStatusLog.builder()
                                .order(order)
                                .ghnOrderCode(order.getGhn().getGhnOrderCode())
                                .status(log.getStatus())
                                .previousStatus(order.getGhnStatus().toString())
                                .ghnUpdatedAt(log.getUpdatedDate())
                                .syncedAt(LocalDateTime.now())
                                .rawData(objectMapper.writeValueAsString(log))
                                .createdAt(LocalDateTime.now())
                                .build();
                ghnOrderStatusLogRepository.save(entity);
            }
        }
    }

}
