package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.*;
import com.example.DATN.constant.Util.CheckSumUtil;
import com.example.DATN.dtos.request.ghtk.GhnOrderInfo;
import com.example.DATN.dtos.request.ghtk.GhnProduct;
import com.example.DATN.dtos.request.order.CheckOutItemRequest;
import com.example.DATN.dtos.request.order.CheckOutRequest;
import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.request.vnpay.VnPayRefundRequest;
import com.example.DATN.dtos.request.vnpay.VnPaymentRequest;
import com.example.DATN.dtos.respone.order.*;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.Embeddable.ShippingAddress;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
    private final VoucherRepository voucherRepository;
    private final VnpayRepository vnpayRepository;
    private final CheckSumUtil checkSumUtil;
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
    private final WareHouseRepository wareHouseRepository;
    private final BigDecimal ORDER_PRICE_2_MILION = BigDecimal.valueOf(2000000);
    private final BigDecimal ORDER_PRICE_5_MILION = BigDecimal.valueOf(5000000);

    @Cacheable(value = "shipping_fee", key = "#address.wardCode")
    public Integer CalculateShippingFee(UserAddress userAddr, ProductVariant variant) {
        if (WeightTier.UP_TO_2KG.getMaxKg() == variant.getWeight()) {
            return 0;
        }

        return 1;
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

    public Integer calculateTotalStock(UUID storeId, ProductVariant variant) {
        Integer quantity = 0;
        List<Stock> stocks = stockRepository.findByVariant(variant);
        for (Stock stock : stocks) {
            if (stock.getStockType() == StockType.STORE) {
                Stock storeStock = stockRepository.findByStore_Id(storeId)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
                quantity += stock.getQuantity();
            }
        }
        return quantity;
    }

    private Store findStoreHasStock(
            ProductVariant variant,
            Integer quantity,
            List<Store> stores
    ) {
        for (Store store : stores) {
            Optional<Stock> stockOpt =
                    stockRepository.findByVariantAndStore(variant, store);

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

    public WareHouse findWarehouse(UserAddress userAddress) {
        Integer province = Integer.parseInt(userAddress.getProvinceCode());
        WareHouse wareHouse = wareHouseRepository.findByProvinceCodeAndIsCentralTrue(province)
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return wareHouse;

    }


    public CheckOutResponse checkOutOrder(CheckOutRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();

        CheckOutResponse response = new CheckOutResponse();
        List<UserAddress> userAddress = userAddressRepository.findByUser(user);
        List<CheckOutProductResponse> checkOutProductResponses = new ArrayList<>();
//        UserAddress userAddr = new UserAddress();
//        for (UserAddress address : userAddress) {
//            if (address.isDefault() == true) {
//                userAddr = address;
//            }
//        }

        for (CheckOutItemRequest checkOutRequest : request.getItem()) {
            ProductVariant productVariant = productVariantRepository.findBysku(checkOutRequest.getSku())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
//            Store store = FindStore(productVariant,checkOutRequest.getQuantity(),userAddr);
//            if (store == null) {
//                WareHouse wareHouse = findWarehouse(userAddr);
//            }
//            WareHouse wareHouse = findWarehouse(userAddr);
//
//            List<String> skus = request.stream()
//                    .map(CheckOutRequest::getSku)
//                    .distinct()
//                    .toList();
//            NearestStoreProjection store = storeRepository
//                    .findNearestStoreWithAllSku(skus, skus.size(), userAddr.getLatitude(), userAddr.getLongitude())
//                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
//            log.info(
//                    "Nearest store: {} - distance: {} km",
//                    store.getName(),
//                    String.format("%.2f", store.getDistanceKm())
//            );
//            log.info(
//                    "Nearest store - id: {}, name: {}, distance: {} km",
//                    store.getId(),
//                    store.getName(),
//                    String.format("%.2f", store.getDistanceKm())
//            );
//            Store nearestStore = storeRepository.findById(store.getId())
//                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            Integer shippingFee = 0;
//          shippingFee = CalculateShippingFee(userAddr, productVariant);
            response.setShippingFee(shippingFee);


            CheckOutProductResponse checkOutResponse = CheckOutProductResponse.builder()
                    .id(productVariant.getId())
                    .isAvailable(productVariant.getIsAvailable())
                    .quantity(checkOutRequest.getQuantity())
                    .sizeName(productVariant.getSize().getName())
                    .colorName(productVariant.getProductColor().getColor().getName())
                    .sku(productVariant.getSku())
                    .productName(productVariant.getProductColor().getProduct().getName())
                    .price(productVariant.getPrice())
                    .finaPrice(productVariant.getPrice().multiply(BigDecimal.valueOf(checkOutRequest.getQuantity())))
                    .stock(100)
                    .imageUrl(productVariant.getProductColor().getImages().isEmpty() ? null :
                            productVariant.getProductColor().getImages().get(0).getImageUrl())
                    .build();
            checkOutProductResponses.add(checkOutResponse);

        }
        response.setProducts(checkOutProductResponses);
        BigDecimal sum = response.getProducts().stream()
                .map(CheckOutProductResponse::getFinaPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if(request.getVoucherCode()!=null) {
            if (!checkSumUtil.verify(request.getVoucherCode().trim())) {
                throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
            }
            sum = CheckVoucher(response,request.getVoucherCode());
        }

        response.setFinalPrice(sum);
        return response;
    }

    public BigDecimal CheckVoucher (CheckOutResponse response,String voucherCode){
        BigDecimal sum = BigDecimal.ZERO;
        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode.trim())
                .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
        switch (voucher.getType()) {
            case PERCENT_DISCOUNT:
                BigDecimal discountAmount = sum
                        .multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
                sum = sum.subtract(discountAmount);
                break;

            case FIXED_AMOUNT:
                sum = sum.compareTo(voucher.getMinOrderValue()) >= 0 ?
                        sum.subtract(voucher.getDiscountValue()) : sum;

                break;

            case FREE_SHIPPING:
                if( response.getShippingFee()!=0)
                {
                    response.setShippingFee(0);
                }else {
                    throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
                }
                break;
            case CASHBACK:
                if (sum.compareTo(voucher.getDiscountValue())>0){
                    sum = sum.subtract(voucher.getDiscountValue());
                }
                else if(sum.compareTo(voucher.getDiscountValue())<0){
                    sum= BigDecimal.ZERO;
                }
                else {
                    throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
                }
                break;

            default:
                throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
        }
        return  sum;
    }
    //    Store findBestStore(
//            List<CheckOutRequest> requests,
//            UserAddress address
//    ){
//        Store bestStore = null;
//        double minDistance = Double.MAX_VALUE;
//
//        for (Store store : allCandidateStores) {
//            boolean đủHàng = true;
//
//            for (CheckOutRequest req : requests) {
//                ProductVariant pv = findBySku(req.getSku());
//                Stock stock = findByStoreAndVariant(store, pv);
//
//                if (stock == null || stock.getQuantity() < req.getQuantity()) {
//                    đủHàng = false;
//                    break;
//                }
//            }
//
//            if (đủHàng) {
//                double distance = calcDistance(address, store);
//                if (distance < minDistance) {
//                    minDistance = distance;
//                    bestStore = store;
//                }
//            }
//        }
//
//    }
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
                    .streetDetail(userAddress.getStreetDetail())
                    .fullDetail(userAddress.getUserAddress())
                    .build();

        }else if(request.getUserAddress() != null){
            shippingAddress = ShippingAddress.builder()
                    .receiverName(request.getUserAddress().getReceiverName())
                    .phoneNumber(request.getUserAddress().getPhoneNumber())
                    .provinceName(request.getUserAddress().getProvinceName())
                    .districtName(request.getUserAddress().getDistrictName())
                    .wardName(request.getUserAddress().getWardName())
                    .streetDetail(request.getUserAddress().getStreetDetail())
                    .fullDetail(request.getUserAddress().getFullDetail())
                    .build();

        }else {
            throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        Order order = new Order();
        order.setUser(user);
        Long code = snowflakeIdGenerator.nextId();
        String orderCode = OrderCodePrefix + code;
        order.setNote(request.getNote());
        order.setOrderCode(orderCode);
        order.setUserAddresses(shippingAddress);
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

    @Transactional(rollbackOn = Exception.class)
    public OrderResponse createOrder
            (OrderRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
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
                    .streetDetail(userAddress.getStreetDetail())
                    .fullDetail(userAddress.getUserAddress())
                    .build();

        }else if(request.getUserAddress() != null){
            shippingAddress = ShippingAddress.builder()
                    .receiverName(request.getUserAddress().getReceiverName())
                    .phoneNumber(request.getUserAddress().getPhoneNumber())
                    .provinceName(request.getUserAddress().getProvinceName())
                    .districtName(request.getUserAddress().getDistrictName())
                    .wardName(request.getUserAddress().getWardName())
                    .streetDetail(request.getUserAddress().getStreetDetail())
                    .fullDetail(request.getUserAddress().getFullDetail())
                    .build();

        }else {
            throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        Order order = new Order();
        order.setUser(user);
        Long code = snowflakeIdGenerator.nextId();
        String orderCode = OrderCodePrefix + code;
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
//                List<Stock> stock = stockRepository.findByVariantAndStore(item,);
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
