package com.example.DATN.services;

import com.example.DATN.constant.Util.CheckSumUtil;
import com.example.DATN.constant.VariantType;
import com.example.DATN.constant.VoucherType;
import com.example.DATN.dtos.request.checkout.*;
import com.example.DATN.dtos.respone.PromotionPriceResponse;
import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.dtos.respone.order.CheckOutProductResponse;
import com.example.DATN.dtos.respone.order.CheckOutResponse;
import com.example.DATN.dtos.respone.order.ShippingAddressRedis;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.StockMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.example.DATN.repositories.projection.CheckOutProjection;
import com.example.DATN.repositories.projection.NearestStoreProjection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckOutService {
    private final UserAddressRepository userAddressRepository;
    private final ProductViewRepository productViewRepository;
    private final StoreRepository storeRepository;
    private final VoucherRepository voucherRepository;
    private final StockRepository stockRepository;
    private final ProductVariantRepository productVariantRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final ShippingCalculator shippingCalculator;
    private final CheckSumUtil checkSumUtil;
    private final StockMapper stockMapper;
    private final ObjectMapper objectMapper;
    private final String PREFIX = "route:v1:";
    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private final RedisTemplate<String, String> redisTemplate;
    private LoadingCache<String, ProductVariant> variantCache;
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

    @PostConstruct
    public void init() {
        variantCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build(sku -> productVariantRepository.findBysku(sku)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND)));
    }

    public String RedisKey(Double lng, Double lat) {
        String key = lat + ":" + lng;
        return PREFIX + key;
    }

//    public ShippingFeeResponse calculateGHNfee(Integer id) {
//        return shippingCalculator.calculateShippingFee(id);
//    }

    private static final String LUA_LOCK_SCRIPT = """
            local userId = ARGV[1]
            local reqQty = tonumber(ARGV[2])
            local sellable = tonumber(ARGV[3])
            local ttl = tonumber(ARGV[4])
            local maxPerUser = tonumber(ARGV[5])
            
            local userLocked = tonumber(redis.call("HGET", KEYS[1], userId) or "0")
            
            -- giới hạn per user
            if reqQty > maxPerUser then
                return -2
            end
            
            local diff = reqQty - userLocked
            local totalLocked = tonumber(redis.call("GET", KEYS[2]) or "0")
            
            -- CASE 1: user tăng số lượng
            if diff > 0 then
                if totalLocked + diff > sellable then
                    return 0 -- OUT OF STOCK
                end
                redis.call("INCRBY", KEYS[2], diff)
            
            -- CASE 2: user giảm số lượng
            elseif diff < 0 then
                redis.call("DECRBY", KEYS[2], -diff)
            end
            
            -- update user lock
            if reqQty > 0 then
                redis.call("HSET", KEYS[1], userId, reqQty)
            else
                redis.call("HDEL", KEYS[1], userId)
            end
            
            redis.call("EXPIRE", KEYS[1], ttl)
            redis.call("EXPIRE", KEYS[2], ttl)
            
            return 1
            
            """;

    public boolean checkVoucherCondition(BigDecimal totalPrice, Voucher voucher) {

        if (!Boolean.TRUE.equals(voucher.getIsActive())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
            return false;
        }

        if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
            return false;
        }

        if (voucher.getMinOrderValue() != null &&
                totalPrice.compareTo(voucher.getMinOrderValue()) < 0) {
            return false;
        }


        return true;
    }

    @Transactional
    public BigDecimal CheckVoucher(CheckOutResponse response,
                                   String voucherCode) {

        BigDecimal sum = response.getTotalPrice();
        BigDecimal discount = BigDecimal.ZERO;
        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode.trim())
                .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
        response.setVoucherId(voucher.getId());
        if (checkVoucherCondition(sum, voucher)) {
            switch (voucher.getType()) {

                case PERCENT_DISCOUNT:
                    discount = sum
                            .multiply(voucher.getDiscountValue())
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);

                    if (voucher.getMaxDiscountValue() != null) {
                        discount = discount.min(voucher.getMaxDiscountValue());
                    }

                    sum = sum.subtract(discount);
                    break;

                case FIXED_AMOUNT:
                    discount = voucher.getDiscountValue();
                    sum = sum.subtract(discount);
                    break;

                case FREE_SHIPPING:
                    discount = response.getShippingFee();
                    response.setShippingFee(BigDecimal.ZERO);
                    break;

                case CASHBACK:
                    discount = voucher.getDiscountValue();
                    break;

                default:
                    throw new ApplicationException(ErrorCode.VOUCHER_CANT_APPLY);
            }
            if (sum.compareTo(BigDecimal.ZERO) < 0) {
                sum = BigDecimal.ZERO;
            }
            response.setType(voucher.getType());
            response.setVoucherDiscount(discount);
            response.setVoucherName(voucher.getVoucherName());
            response.setTotalPrice(sum);
        }
        return sum;
    }


    @Async
    public void AddView(ProductVariant variant, User user) {
        UUID productId = variant.getProductColor().getProduct().getId();
        ProductView view = ProductView.builder().variant(variant).variantType(VariantType.CART).user(user).build();
        productViewRepository.save(view);
        increaseView(variant.getId(), productId);
    }

    @Async
    public void increaseView(UUID variantId, UUID product) {
        String day = DAY_FMT.format(Instant.now());
        String key = "product:view:variant:" + variantId + ":" + day;
        String keyProduct = "product:view:product:" + product + ":" + day;
        redisTemplate.opsForValue().increment(keyProduct, 1);
        redisTemplate.expire(keyProduct, Duration.ofDays(2));
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, Duration.ofDays(2));
    }

    private String buildRouteCacheKey(
            double lat,
            double lng,
            Map<String, Integer> quantities
    ) {
        double latKey = roundGrid2Km(lat);
        double lngKey = roundGrid2Km(lng);

        String skuHash = quantities.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining("|"))
                .hashCode() + "";

        return "route:" + latKey + ":" + lngKey + ":" + skuHash;
    }

    public CheckOutProjection resolveRoute(
            List<String> skus, Map<String, Integer> quantities,
            double lat, double lng) throws JsonProcessingException {

        double latKey = roundGrid2Km(lat);
        double lngKey = roundGrid2Km(lng);

        Optional<NearestStoreProjection> nearest =
                storeRepository.findNearestStoreWithAllSku(
                        skus,
                        skus.size(),
                        lat,
                        lng
                );
        if (nearest.isPresent()) {
            return CheckOutProjection.builder()
                    .storeId(nearest.get().getId())
                    .distanceKm(nearest.get().getDistanceKm())
                    .build();
        }

        return CheckOutProjection.builder()
                .storeId(null)
                .distanceKm(0.0)
                .build();
    }

    private double roundGrid2Km(double value) {
        return Math.round(value * 50.0) / 50.0;
    }

    @Transactional(readOnly = true)
    public ProductVariant getVariant(String sku) {

        return variantCache.get(sku);
    }

    private Map<String, ProductVariant> batchLoadVariants(List<String> skus) {
        Map<String, ProductVariant> result = new HashMap<>();
        List<String> cacheMisses = new ArrayList<>();

        for (String sku : skus) {
            try {
                ProductVariant variant = getVariant(sku);
                if (variant != null) {
                    result.put(sku, variant);
                } else {
                    cacheMisses.add(sku);
                }
            } catch (Exception e) {
                cacheMisses.add(sku);
            }
        }

        if (!cacheMisses.isEmpty()) {
            List<ProductVariant> loaded = productVariantRepository
                    .findAllBySkuIn(cacheMisses);

            for (ProductVariant v : loaded) {
                result.put(v.getSku(), v);

                variantCache.put(v.getSku(), v);
            }
        }

        return result;
    }

    public void DeleteIdempotencyKey(String idempotencyKey) {
        try {
            CheckOutResponse cacheResponse = getIdempotentResponse(idempotencyKey);
            if (cacheResponse != null) {
                redisTemplate.delete(idempotencyKey);
            }
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.IDEMPOTENCY_TIMEOUT);
        }
    }

    @Transactional(readOnly = true)
    public CheckOutResponse applyVoucher(ApplyVoucherRequest request) {
        String idempotencyKey = request.getIdempotencyKey();
        CheckOutResponse cacheResponse = getIdempotentResponse(idempotencyKey);
        if (cacheResponse != null) {
            if (cacheResponse.getVoucherCode().equals(request.getVoucherCode())) {
                return cacheResponse;
            } else {
                if (cacheResponse.getVoucherId() != null) {
                    Voucher voucher = voucherRepository.findByVoucherCode(request.getVoucherCode()
                                    .trim())
                            .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
                    if (cacheResponse.getVoucherName().equals(voucher.getVoucherName())) {
                        return cacheResponse;
                    }
                }
            }

            cacheResponse.setTotalPrice(cacheResponse.getOriginTotalPrice());
            cacheResponse.setShippingFee(cacheResponse.getOriginalShippingFee());

            if (request.getVoucherCode() != null) {
                if (!checkSumUtil.verify(request.getVoucherCode().trim())) {
                    throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
                }
                try {
                    BigDecimal discountValue = CheckVoucher(cacheResponse, request.getVoucherCode());
                } catch (Exception e) {
                    throw new ApplicationException(ErrorCode.VOUCHER_CANT_APPLY);
                }
                cacheResponse.setVoucherCode(request.getVoucherCode());
                cacheResponse.setOriginalShippingFee(cacheResponse.getOriginalShippingFee());
                cacheResponse.setOriginTotalPrice(cacheResponse.getTotalPrice());
                cacheResponse.setFinalPrice(cacheResponse.getTotalPrice().add(cacheResponse.getShippingFee()));
                saveIdempotentResponse(request.getIdempotencyKey(), cacheResponse);
            }
            return cacheResponse;
        } else {
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }

    }


    public Map<UUID, Boolean> checkStockFromStore(Map<String, Stock> stockMap
            , List<String> skus
            , Map<String, Integer> quantities) {
        List<Stock> stocks = skus.stream()
                .map(stockMap::get)
                .filter(Objects::nonNull)
                .toList();

        Map<Store, List<Stock>> stockByStore = stocks.stream()
                .collect(Collectors.groupingBy(Stock::getStore));
        Map<UUID, Boolean> result = new HashMap<>();
        for (Map.Entry<Store, List<Stock>> entry : stockByStore.entrySet()) {
            Store storeId = entry.getKey();
            List<Stock> storeStocks = entry.getValue();
            boolean canFulfill = storeStocks.stream()
                    .allMatch(stock -> {
                        String sku = stock.getVariant().getSku();
                        int requiredQty = quantities.getOrDefault(sku, 0);
                        return stock.getSellableQuantity() >= requiredQty;
                    });
            result.put(storeId.getId(), canFulfill);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckOutResponse processCheckoutInternal(CheckOutRequest request)
            throws JsonProcessingException {
        String idempotencyKey = request.getIdempotencyKey();
        CheckOutResponse cacheResponse = getIdempotentResponse(idempotencyKey);
        if (cacheResponse != null) {
            return cacheResponse;
        }
        User user = getUserByJwtHelper.getCurrentUser();
        List<String> skus = request.getItem().stream()
                .map(CheckOutItemRequest::getSku)
                .toList();
        Map<String, ProductVariant> variants = batchLoadVariants(skus);
        Map<String, Integer> quantities = request.getItem().stream()
                .collect(Collectors.toMap(
                        CheckOutItemRequest::getSku,
                        CheckOutItemRequest::getQuantity
                ));
        Set<String> notFound = skus.stream()
                .filter(sku -> !variants.containsKey(sku))
                .collect(Collectors.toSet());
        if (!notFound.isEmpty()) {
            throw new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND,
                    "SKUs not found: " + notFound);
        }
        String lockKey = IDEMPOTENCY_PREFIX + "lock:" + idempotencyKey;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(acquired)) {
            log.info(" Waiting for concurrent request: {}", idempotencyKey);
            return waitForIdempotentResponse(idempotencyKey);
        }
        UserAddress address = user.getUserAddress().stream()
                .filter(UserAddress::isDefault)
                .findFirst()
                .orElse(null);
        CheckOutProjection route = new CheckOutProjection();
        if (address != null) {
            route = resolveRoute(skus,
                    quantities, address.getLatitude(), address.getLongitude());
            double distance = route.getDistanceKm() * 1000;
            double rounded = Math.round(distance * 100.0) / 100.0;
            route.setDistanceKm(rounded);

        }
        CheckOutResponse response = new CheckOutResponse();
        Map<String, Stock> stocks = batchLoadStocks(variants, quantities, route);
        if (stocks.isEmpty()) {
            throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
        } else {
            Map<UUID, Boolean> result = checkStockFromStore(stocks, skus, quantities);
            Optional<UUID> storeIdOpt = result.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .findFirst();

            if (storeIdOpt.isEmpty()) {
                throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
            }

            response.setStoreId(storeIdOpt.get());

        }
        Map<String, Boolean> lockResults = batchLockStocks(
                request.getItem(), stocks, user.getId()
        );

        List<String> failedLocks = lockResults.entrySet().stream()
                .filter(e -> !e.getValue())
                .map(Map.Entry::getKey)
                .toList();

        if (!failedLocks.isEmpty()) {
            rollbackLocks(lockResults, user.getId());
            throw new ApplicationException(ErrorCode.OUT_OF_STOCK,
                    "Out of stock for SKUs: " + failedLocks);
        }
        response = checkOutOrder(
                request, stocks, address, response, quantities, variants, route);

        if (address != null) {
            ShippingAddressRedis shippingAddress = ShippingAddressRedis.builder()
                    .receiverName(address.getReceiverName())
                    .phoneNumber(address.getPhoneNumber())
                    .provinceName(address.getProvinceName())
                    .district_Id(address.getDistrictCode())
                    .districtName(address.getDistrictName())
                    .wardCode(address.getWardCode())
                    .wardName(address.getWardName())
                    .streetDetail(address.getStreetDetail())
                    .fullDetail(address.getUserAddress())
                    .longitude(address.getLongitude())
                    .latitude(address.getLatitude())
                    .build();
            response.setShippingAddressResponse(shippingAddress);

        } else {
            response.setMessageForUser("please add the address or fill " +
                    "an address form to find the nearest store ");
            response.setShippingAddressResponse(null);
        }
        saveIdempotentResponse(request.getIdempotencyKey(), response);

        return response;

    }

    public ShippingAddressRedis mapToShippingByUserAddress(UserAddress address) {
        ShippingAddressRedis shippingAddress = ShippingAddressRedis.builder()
                .receiverName(address.getReceiverName())
                .phoneNumber(address.getPhoneNumber())
                .provinceName(address.getProvinceName())
                .district_Id(address.getDistrictCode())
                .districtName(address.getDistrictName())
                .wardCode(address.getWardCode())
                .wardName(address.getWardName())
                .streetDetail(address.getStreetDetail())
                .fullDetail(address.getUserAddress())
                .longitude(address.getLongitude())
                .latitude(address.getLatitude())
                .build();
        return shippingAddress;
    }

    public CheckOutResponse calculatorDistance(DistanceRequest request) throws JsonProcessingException {
        User user = getUserByJwtHelper.getCurrentUser();
        String idempotencyKey = request.getIdempotencyKey();
        CheckOutResponse response = getIdempotentResponse(idempotencyKey);
        if (response != null) {
            List<String> skus = response.getProducts().stream().map(
                    item -> item.getSku()).toList();
            Map<String, Integer> quantities = response.getProducts().stream()
                    .collect(Collectors.toMap(
                            CheckOutProductResponse::getSku,
                            CheckOutProductResponse::getQuantity
                    ));
            Map<String, ProductVariant> variants = batchLoadVariants(skus);
            Set<String> notFound = skus.stream()
                    .filter(sku -> !variants.containsKey(sku))
                    .collect(Collectors.toSet());
            if (!notFound.isEmpty()) {
                throw new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND,
                        "SKUs not found: " + notFound);
            }
            if (request.getUserAddressId() == null) {
                response.setShippingAddressResponse(request.getShippingAddressRedis());
            } else {
                if (response.getUserAddressId() != request.getUserAddressId()) {
                    UserAddress address = userAddressRepository.findById(request.getUserAddressId())
                            .orElseThrow(() -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND));
                    ShippingAddressRedis shippingAddress = mapToShippingByUserAddress(address);
                    response.setShippingAddressResponse(shippingAddress);
                }
            }
            BigDecimal shippingFee = BigDecimal.ZERO;
            CheckOutProjection checkOutProjection = resolveRoute(skus,
                    quantities, response.getShippingAddressResponse().getLatitude(), response.getShippingAddressResponse().getLongitude());
            double distance = checkOutProjection.getDistanceKm() * 1000;
            double rounded = Math.round(distance * 100.0) / 100.0;
            checkOutProjection.setDistanceKm(rounded);
            if (checkOutProjection.getStoreId() != null) {
                Store store = storeRepository.findById(checkOutProjection.getStoreId())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
                response.setShippingFee(shippingFee);

            } else {

                throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
            }
            Map<String, Stock> stockMap = batchLoadStocks(variants
                    , quantities, checkOutProjection);
            Map<String, Boolean> lockResults = batchReLockStock(
                    response, stockMap, user.getId()
            );
            List<String> failedLocks = lockResults.entrySet().stream()
                    .filter(e -> !e.getValue())
                    .map(Map.Entry::getKey)
                    .toList();
            if (stockMap.isEmpty()) {
                throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
            } else {
                Map<UUID, Boolean> result = checkStockFromStore(stockMap, skus, quantities);
                Optional<UUID> storeIdOpt = result.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .findFirst();
                if (storeIdOpt.isEmpty()) {
                    throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
                }
            }
            if (!failedLocks.isEmpty()) {
                rollbackLocks(lockResults, user.getId());
                throw new ApplicationException(ErrorCode.OUT_OF_STOCK,
                        "Out of stock for SKUs: " + failedLocks);
            }

            Store store = storeRepository.findById(checkOutProjection.getStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            response.setStoreId(store.getId());
            calculateCheckoutPrices(response, variants, quantities);
            if (response.getType() != VoucherType.FREE_SHIPPING) {
                shippingFee = shippingCalculator.recalculatorShippingAddress(store, response);
                response.setOriginalShippingFee(shippingFee);

                response.setShippingFee(shippingFee);
            } else {
                response.setShippingFee(shippingFee);
            }
            response.setFinalPrice(response.getTotalPrice().add(shippingFee));

            saveIdempotentResponse(idempotencyKey, response);
            return response;
        }

        throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);

    }


    public CheckOutResponse calculatorItem(List<IncreaseQuantityRequest> request, String idempotencyKey) throws JsonProcessingException {

        User user = getUserByJwtHelper.getCurrentUser();
        CheckOutResponse response = getIdempotentResponse(idempotencyKey);
        BigDecimal sum = BigDecimal.ZERO;
        Map<String, Stock> stockMap = new HashMap<>();
        List<String> skus = new ArrayList<>();
        Map<String, Integer> quantities = new HashMap<>();
        BigDecimal shippingFee = BigDecimal.ZERO;
        if (response != null) {
            for (IncreaseQuantityRequest quantityRequest : request) {
                response.getProducts().stream()
                        .filter(item -> item.getSku().equals(quantityRequest.getSku()))
                        .findFirst()
                        .ifPresent(item ->
                                item.setQuantity(quantityRequest.getQuantity() + item.getQuantity()
                                )
                        );
                for (CheckOutProductResponse productResponse : response.getProducts()) {
                    BigDecimal productFinalPrice = productResponse.getDiscountPrice()
                            .multiply(BigDecimal.valueOf(productResponse.getQuantity()));
                    productResponse.setFinalPrice(productFinalPrice);
                    Stock stock = stockRepository.findById(productResponse.getStockResponse().getId())
                            .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
                    stockMap.put(productResponse.getSku(), stock);
                    skus.add(productResponse.getSku());
                    quantities.put(productResponse.getSku(), productResponse.getQuantity());
                    sum = sum.add(productFinalPrice);
                }
            }

            if (response.getStoreId() != null || response.getStockId() != null) {
                if (stockMap.isEmpty()) {
                    throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
                } else {
                    Map<UUID, Boolean> result = checkStockFromStore(stockMap, skus, quantities);
                    Optional<UUID> storeIdOpt = result.entrySet().stream()
                            .filter(Map.Entry::getValue)
                            .map(Map.Entry::getKey)
                            .findFirst();
                    if (storeIdOpt.isEmpty()) {
                        if (response.getShippingAddressResponse() == null) {
                            response.setMessageForUser("please add the address or fill " +
                                    "an address form to find the nearest store ");

                            return response;
                        } else {
                            throw new ApplicationException(ErrorCode.OUT_OF_STOCK);

                        }
                    } else {
                        response.setStoreId(storeIdOpt.get());
                    }
                }
                response.setOriginTotalPrice(sum);
                response.setTotalPrice(sum);
                if (response.getVoucherId() != null) {
                    if (checkSumUtil.verify(response.getVoucherCode())) {
                        BigDecimal discountValue = CheckVoucher(response,
                                response.getVoucherCode());
                    }
                }
                if (response.getShippingAddressResponse() != null) {
                    shippingFee = shippingCalculator.recalculatorItem(response);
                }
                response.setOriginalShippingFee(shippingFee);
                response.setShippingFee(shippingFee);
                response.setFinalPrice(response.getTotalPrice().add(shippingFee));

                Map<String, Boolean> lockStock = batchReLockStock(response, stockMap, user.getId());
                List<String> failedLocks = lockStock.entrySet().stream()
                        .filter(e -> !e.getValue())
                        .map(Map.Entry::getKey)
                        .toList();

                if (!failedLocks.isEmpty()) {
                    rollbackLocks(lockStock, user.getId());
                    throw new ApplicationException(ErrorCode.OUT_OF_STOCK,
                            "Out of stock for SKUs: " + failedLocks);
                }
                saveIdempotentResponse(idempotencyKey, response);
                return response;
            }
        } else {
            throw new ApplicationException(ErrorCode.OUT_OF_STOCK);
        }
        throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
    }


    private Map<String, Stock> batchLoadStocks(
            Map<String, ProductVariant> variants,
            Map<String, Integer> quantities,
            CheckOutProjection route) {

        List<UUID> variantIds = variants.values().stream()
                .map(ProductVariant::getId)
                .toList();
        List<Stock> stocks = new ArrayList<>();
        if (route.getStoreId() != null) {
            Store store = storeRepository.findById(route.getStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            stocks = stockRepository.findAllByVariantIdsAndStore(variantIds, store);
        } else {
            List<Stock> allValidStock = stockRepository.findAllByVariantIdsAndStockType(variantIds);
            stocks = allValidStock.stream()
                    .filter(s -> {
                        Integer requiredQty = quantities.get(s.getVariant().getSku());
                        return requiredQty != null
                                && s.getSellableQuantity() >= requiredQty;
                    })
                    .toList();

        }

        return stocks.stream()
                .collect(Collectors.toMap(
                        s -> s.getVariant().getSku(),
                        s -> s,
                        (s1, s2) -> s1.getSellableQuantity() >= s2.getSellableQuantity()
                                ? s1 : s2
                ));
    }

    private void rollbackLocks(
            Map<String, Boolean> lockResults,
            Long userId) {

        lockResults.entrySet().stream()
                .filter(Map.Entry::getValue)
                .forEach(entry -> {
                    String key = "stock_lock:" + entry.getKey();
                    redisTemplate.opsForHash().delete(key, String.valueOf(userId));
                });
    }


    @Transactional(readOnly = true)
    public CheckOutResponse checkOutOrder(CheckOutRequest request
            , Map<String, Stock> stockMap
            , UserAddress address
            , CheckOutResponse response
            , Map<String, Integer> quantites
            , Map<String, ProductVariant> variantMap
            , CheckOutProjection route
    ) throws JsonProcessingException {
        Integer lockQuantity = 0;
        Integer total_weight = 0;
        Integer total_length = 0;
        Integer total_width = 0;
        Integer total_height = 0;

        BigDecimal fee = BigDecimal.ZERO;
        List<CheckOutProductResponse> checkOutProductResponses = new ArrayList<>();
        Map<String, Integer> skuQuantities = request.getItem().stream()
                .collect(Collectors.toMap(
                        CheckOutItemRequest::getSku,
                        CheckOutItemRequest::getQuantity
                ));

        for (CheckOutItemRequest checkOutItemRequest : request.getItem()) {
            ProductVariant productVariant = getVariant(checkOutItemRequest.getSku());
            Integer quantity = checkOutItemRequest.getQuantity();
            Stock stock = stockMap.get(productVariant.getSku());
            total_weight += productVariant.getWeight() * quantity;
            total_height += productVariant.getHeight() * quantity;
            total_length = Math.max(total_length, productVariant.getLength());
            total_width = Math.max(total_width, productVariant.getWidth());
            StockResponse stockResponse = stockMapper.toStockResponse(stock);
            response.setStockId(stock.getId());
            skuQuantities.put(checkOutItemRequest.getSku(), checkOutItemRequest.getQuantity());
            String totalKey = "stock_lock_total:" + stock.getId();
            String value = redisTemplate.opsForValue().get(totalKey);
            lockQuantity = Integer.parseInt(value != null ? value : "0");
            response.setDistance(route.getDistanceKm());
            CheckOutProductResponse checkOutResponse = CheckOutProductResponse.builder()
                    .id(productVariant.getId())
                    .isAvailable(productVariant.getIsAvailable())
                    .quantity(checkOutItemRequest.getQuantity())
                    .sizeName(productVariant.getSize().getName())
                    .colorName(productVariant.getProductColor().getColor().getName())
                    .sku(productVariant.getSku())
                    .productName(productVariant.getProductColor().getProduct().getName()).originalPrice(productVariant.getPrice())
                    .finalPrice(productVariant.getPrice().multiply(BigDecimal.valueOf(checkOutItemRequest.getQuantity())))
                    .stockResponse(stockResponse)
                    .weight(productVariant.getWeight())
                    .height(productVariant.getHeight())
                    .length(productVariant.getLength())
                    .width(productVariant.getWidth())
                    .stock(stock.getSellableQuantity() - lockQuantity)
                    .imageUrl(productVariant.getProductColor().getImages().isEmpty()
                            ? null : productVariant.getProductColor().getImages().get(0).getImageUrl())
                    .build();
            checkOutProductResponses.add(checkOutResponse);
        }
        response.setProducts(checkOutProductResponses);
        BigDecimal sum = response.getProducts().stream()
                .map(CheckOutProductResponse::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        total_weight = (int) Math.ceil(total_weight * 1.1);
        response.setVoucherCode("chưa có voucher");
        response.setTotal_weight(total_weight);
        response.setTotal_height(total_height);
        response.setTotal_width(total_width);
        response.setTotal_length(total_length);
        if (address != null) {
            fee = shippingCalculator
                    .calculatorShippingFee(skuQuantities,
                            stockMap, variantMap, address, route, response);
        }
        response.setTotalPrice(sum);
        calculateCheckoutPrices(response, variantMap, quantites);
        response.setOriginTotalPrice(response.getTotalPrice());
        response.setOriginalShippingFee(fee);
        response.setShippingFee(fee);
        response.setFinalPrice(response.getTotalPrice().add(fee));
        saveIdempotentResponse(request.getIdempotencyKey(), response);
        return response;
    }

    public PromotionPriceResponse getVariantPrice(String sku) throws JsonProcessingException {

        String key = "PROMO:VARIANT:" + sku;

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        PromotionPriceResponse response =
                objectMapper.readValue(
                        value.toString(),
                        PromotionPriceResponse.class
                );
        ;
        return response;
    }

    public void calculateCheckoutPrices(
            CheckOutResponse response,
            Map<String, ProductVariant> variantMap,
            Map<String, Integer> quantities
    ) throws JsonProcessingException {
        BigDecimal subTotal = BigDecimal.ZERO;

        for (CheckOutProductResponse productResponse : response.getProducts()) {
            String sku = productResponse.getSku();
            int quantity = productResponse.getQuantity();

            ProductVariant variant = variantMap.get(sku);

            BigDecimal originalPrice = variant.getPrice();
            BigDecimal unitPrice = originalPrice;
            BigDecimal discountPrice = null;

            PromotionPriceResponse redisPrice = getVariantPrice(sku);

            if (redisPrice != null) {
                originalPrice = redisPrice.getOriginalPrice();
                unitPrice = redisPrice.getDiscountPrice();
                discountPrice = redisPrice.getDiscountPrice();
            }

            BigDecimal finalPrice =
                    unitPrice.multiply(BigDecimal.valueOf(quantity));

            subTotal = subTotal.add(finalPrice);
            productResponse.setOriginalPrice(originalPrice);
            productResponse.setDiscountPrice(discountPrice);
            productResponse.setFinalPrice(finalPrice);
        }
        response.setTotalPrice(subTotal);
    }

    private CheckOutResponse waitForIdempotentResponse(String key) {
        int maxRetries = 10;
        int retryDelayMs = 500;

        for (int i = 0; i < maxRetries; i++) {
            CheckOutResponse response = getIdempotentResponse(key);
            if (response != null) {
                return response;
            }

            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ApplicationException(ErrorCode.CONCURRENT_REQUEST_ERROR);
            }
        }

        throw new ApplicationException(ErrorCode.IDEMPOTENCY_TIMEOUT);
    }

    private void saveIdempotentResponse(String key, CheckOutResponse response) {
        try {
            String cacheKey = IDEMPOTENCY_PREFIX + key;
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(10));

            log.info("Saved idempotent response for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to save idempotent response for key: {}", key, e);
        }
    }

    private Map<String, Boolean> batchLockStocks(
            List<CheckOutItemRequest> items,
            Map<String, Stock> stocks,
            Long userId) {

        Map<String, Boolean> results = new HashMap<>();
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_LOCK_SCRIPT);
        script.setResultType(Long.class);

        for (CheckOutItemRequest item : items) {
            String sku = item.getSku();
            Stock stock = stocks.get(sku);

            if (stock == null) {
                results.put(sku, false);
                continue;
            }
            String lockKey = "stock_lock:" + stock.getId();
            String totalKey = "stock_lock_total:" + stock.getId();
            Long result = redisTemplate.execute(
                    script,
                    List.of(lockKey, totalKey),
                    String.valueOf(userId),                    // ARGV[1]
                    String.valueOf(item.getQuantity()),        // ARGV[2]
                    String.valueOf(stock.getSellableQuantity()), // ARGV[3]
                    "600",                                    // ARGV[4]
                    "5"                                        // ARGV[5]
            );
            results.put(stock.getId().toString(), result != null && result == 1);
        }
        return results;
    }

    private Map<String, Boolean> batchReLockStock(
            CheckOutResponse items,
            Map<String, Stock> stocks,
            Long userId) {

        Map<String, Boolean> results = new HashMap<>();
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_LOCK_SCRIPT);
        script.setResultType(Long.class);

        for (CheckOutProductResponse item : items.getProducts()) {
            String sku = item.getSku();
            Stock stock = stocks.get(sku);

            if (stock == null) {
                results.put(sku, false);
                continue;
            }
            String lockKey = "stock_lock:" + stock.getId();
            String totalKey = "stock_lock_total:" + stock.getId();
            Long result = redisTemplate.execute(
                    script,
                    List.of(lockKey, totalKey),
                    String.valueOf(userId),                    // ARGV[1]
                    String.valueOf(item.getQuantity()),        // ARGV[2]
                    String.valueOf(stock.getSellableQuantity()), // ARGV[3]
                    "600",                                    // ARGV[4]
                    "5"                                        // ARGV[5]
            );
            String value = redisTemplate.opsForValue().get(totalKey);
            Integer lockQuantity = Integer.parseInt(value != null ? value : "0");
            item.setStock(stock.getSellableQuantity() - lockQuantity);
            results.put(stock.getId().toString(), result != null && result == 1);
        }
        return results;
    }

    public Map<Long, Integer> getAllLockedStock(String sku, UUID storeId) {
        String key = "stock_lock:" + sku + ":" + storeId;

        Map<Object, Object> redisResult = redisTemplate.opsForHash().entries(key);
        Map<Long, Integer> lockedStock = new HashMap<>();
        for (Map.Entry<Object, Object> entry : redisResult.entrySet()) {
            long uid = Long.parseLong(entry.getKey().toString());
            int qty = Integer.parseInt(entry.getValue().toString());
            lockedStock.put(uid, qty);
        }

        return lockedStock;
    }

    public void unlockStock(String sku, UUID storeId, long userId) {
        String key = "stock_lock:" + sku + ":" + storeId;
        redisTemplate.opsForHash().delete(key, String.valueOf(userId));
    }

}



