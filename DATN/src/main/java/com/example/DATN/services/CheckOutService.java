package com.example.DATN.services;

import com.example.DATN.constant.Util.CheckSumUtil;
import com.example.DATN.constant.VariantType;
import com.example.DATN.dtos.request.checkout.*;
import com.example.DATN.dtos.respone.order.CheckOutProductResponse;
import com.example.DATN.dtos.respone.order.CheckOutResponse;
import com.example.DATN.dtos.respone.order.ShippingAddressRedis;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.OrderMapper;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckOutService {
    private final OrderMapper orderMapper;
    private final WareHouseRepository wareHouseRepository;
    private final ProductViewRepository productViewRepository;
    private final StoreRepository storeRepository;
    private final VoucherRepository voucherRepository;
    private final StockRepository stockRepository;
    private final ProductVariantRepository productVariantRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final ShippingCalculator shippingCalculator;
    private final CheckSumUtil checkSumUtil;
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

    private static final String LUA_LOCK_SCRIPT = """
            local userId = ARGV[1]
              local reqQty = tonumber(ARGV[2])
              local sellable = tonumber(ARGV[3])
              local ttl = tonumber(ARGV[4])
              local maxPerUser = tonumber(ARGV[5])
            
              -- số lượng user đã lock trước đó
              local userLocked = tonumber(redis.call("HGET", KEYS[1], userId) or "0")
            
              -- giới hạn per user
              if userLocked + reqQty > maxPerUser then
                  return -2
              end
            
              -- chỉ lock phần CHÊNH LỆCH
              local need = reqQty - userLocked
              if need <= 0 then
                  -- user chỉ checkout lại, refresh, KHÔNG trừ thêm
                  redis.call("EXPIRE", KEYS[1], ttl)
                  redis.call("EXPIRE", KEYS[2], ttl)
                  return 1
              end
            
              -- tổng đã lock
              local totalLocked = tonumber(redis.call("GET", KEYS[2]) or "0")
            
              -- kiểm tra stock
              if totalLocked + need > sellable then
                  return 0 -- OUT OF STOCK
              end
            
              -- update lock
              redis.call("HSET", KEYS[1], userId, reqQty)
              redis.call("INCRBY", KEYS[2], need)
            
              redis.call("EXPIRE", KEYS[1], ttl)
              redis.call("EXPIRE", KEYS[2], ttl)
            
              return 1
            """;

    @Transactional
    public BigDecimal CheckVoucher(CheckOutResponse response, String voucherCode, BigDecimal price) {
        BigDecimal sum = price;

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode.trim())
                .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));

        switch (voucher.getType()) {
            case PERCENT_DISCOUNT:
                BigDecimal discountAmount = sum
                        .multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
                sum = sum.subtract(discountAmount);
                break;
            case FIXED_AMOUNT:
                sum = sum.compareTo(voucher.getMinOrderValue()) >= 0
                        ? sum.subtract(voucher.getDiscountValue()) : sum;
                break;
            case FREE_SHIPPING:
                if (response.getShippingFee().compareTo(BigDecimal.ZERO) > 0) {
                    response.setShippingFee(BigDecimal.ZERO);
                } else {
                    throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
                }
                break;
            case CASHBACK:
                sum = sum.subtract(voucher.getDiscountValue());
                if (sum.compareTo(BigDecimal.ZERO) < 0) {
                    sum = BigDecimal.ZERO;
                }
                break;
            default:
                throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
        }
        return sum.setScale(0, RoundingMode.DOWN);
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

    public Stock CheckStock(Map<String, Integer> variantMap, List<WareHouse> wareHouse) {
        for (WareHouse ware : wareHouse) {
            for (Map.Entry<String, Integer> variant : variantMap.entrySet()) {
                ProductVariant productVariant = variantCache.get(variant.getKey());
                Stock stock = stockRepository
                        .findByVariant_IdAndWarehouse(productVariant.getId(), ware)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));

                if (stock.getSellableQuantity() >= variant.getValue()) {
                    return stock;
                } else continue;
            }
        }
        return null;
    }

    public CheckOutProjection resolveRoute(List<String> skus,
                                           double lat, double lng) throws JsonProcessingException {

        double latKey = roundGrid2Km(lat);
        double lngKey = roundGrid2Km(lng);
        String cacheKey = RedisKey(lngKey, latKey);
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            CheckOutProjection projection = objectMapper.readValue(cached, CheckOutProjection.class);
            return projection;
        }
        Optional<NearestStoreProjection> nearest = storeRepository
                .findNearestStoreWithAllSku(skus, skus.size(), lat, lng);
        CheckOutProjection route = new CheckOutProjection();
        if (nearest.isEmpty() || nearest.get().getDistanceKm() > 10) {
            route = route.ghn();
        } else {
            double distanceRounded = Math.round(nearest.get().getDistanceKm() * 100.0) / 100.0;
            route = route.store(nearest.get().getId(), distanceRounded);
        }
        String json = objectMapper.writeValueAsString(route);
        redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(30));
        return route;
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
                ProductVariant variant = variantCache.getIfPresent(sku);
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
        try {
            String idempotencyKey = request.getIdempotencyKey();
            CheckOutResponse cacheResponse = getIdempotentResponse(idempotencyKey);
            if (cacheResponse != null) {
                BigDecimal sum = cacheResponse.getProducts()
                        .stream().map(CheckOutProductResponse::getFinaPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (request.getVoucherCode() != null) {
                    if (!checkSumUtil.verify(request.getVoucherCode().trim())) {
                        throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
                    }
                    sum = CheckVoucher(cacheResponse, request.getVoucherCode(), sum);
                    cacheResponse.setVoucherCode(request.getVoucherCode());
                    cacheResponse.setVoucherDiscount(cacheResponse.getFinalPrice().subtract(sum));
                    cacheResponse.setFinalPrice(sum);
                    saveIdempotentResponse(request.getIdempotencyKey(), cacheResponse);

                }
                return cacheResponse;
            }

        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.IDEMPOTENCY_TIMEOUT);
        }
        return null;
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
            CheckOutProjection route = resolveRoute(skus,
                    address.getLatitude(), address.getLongitude());


        Map<String, Stock> stocks = batchLoadStocks(variants, quantities,route);

        Map<String, Boolean> lockResults = batchLockStocks(
                request.getItem(), stocks, route, user.getId()
        );

        List<String> failedLocks = lockResults.entrySet().stream()
                .filter(e -> !e.getValue())
                .map(Map.Entry::getKey)
                .toList();

        if (!failedLocks.isEmpty()) {
            rollbackLocks(lockResults, route, user.getId());
            throw new ApplicationException(ErrorCode.OUT_OF_STOCK,
                    "Out of stock for SKUs: " + failedLocks);
        }


        CheckOutResponse response = checkOutOrder(
                request,stocks, route);
        if(address !=null){
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
        }
        else {
            response.setShippingAddressResponse(null);
        }

        saveIdempotentResponse(request.getIdempotencyKey(), response);

        return response;
    }

    public CheckOutResponse calculatorDistance(DistanceRequest request){
        try {
            String idempotencyKey = request.getIdempotencyKey();
            CheckOutResponse response = getIdempotentResponse(idempotencyKey);
            if (response != null) {
                List<String> skus = response.getProducts().stream().map(
                        item->item.getSku()
                ).toList();
                CheckOutProjection checkOutProjection =  resolveRoute(skus,
                        request.getLat(), request.getLng());
                BigDecimal distanceFee =shippingCalculator.calculateShippingFeeByDistance
                        (checkOutProjection.getDistanceKm());
                response.setDistanceFee(distanceFee);
                BigDecimal total = PlusFee(distanceFee,response.getQuantityFee(),response.getWeightFee());
                response.setFinalPrice(total);
                return response;
            }
        }catch (Exception e){
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        return null;

    }

    public CheckOutResponse calculatorItem (IncreaseQuantityRequest request){
        try {
            int total_quantity=0;
            String idempotencyKey = request.getIdempotencyKey();
            CheckOutResponse response = getIdempotentResponse(idempotencyKey);
            Map<String,Integer> variantQuantity = new HashMap<>();
            if (response != null) {
                for(CheckOutProductResponse productResponse : response.getProducts()){
                    total_quantity += productResponse.getQuantity();
                    if(productResponse.getSku().equals(request.getSku())) {
                        productResponse.setQuantity(request.getQuantity());
                        variantQuantity.put(productResponse.getSku(), request.getQuantity());
                    }
                    variantQuantity.put(productResponse.getSku(),productResponse.getQuantity());
                }
                BigDecimal variantFee = shippingCalculator
                        .calculatorShippingFeeByWeight(variantQuantity);
                BigDecimal quantityFee= BigDecimal.valueOf(1000)
                        .multiply(BigDecimal.valueOf(total_quantity));
                response.setWeightFee(variantFee);
                response.setQuantityFee(quantityFee);
                response.setShippingFee(PlusFee(variantFee,quantityFee,response.getDistanceFee()));
                return response;
            }
        }catch (Exception e){
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        return null;
    }
    private BigDecimal PlusFee(BigDecimal weightFee
            ,BigDecimal quantityFee, BigDecimal DistanceFee)
    {
        return weightFee.add(quantityFee.add(DistanceFee));
    }
    private Map<String, Stock> batchLoadStocks(
            Map<String, ProductVariant> variants,
            Map<String,Integer> quantities,
            CheckOutProjection route) {

        List<UUID> variantIds = variants.values().stream()
                .map(ProductVariant::getId)
                .toList();
        List<Stock> stocks = new ArrayList<>();
        if (route.isStore()) {
            Store store = storeRepository.findById(route.getStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

            stocks = stockRepository.findAllByVariantIdsAndStore(variantIds, store);
        } else {
            List<WareHouse> wareHouse = wareHouseRepository.findAll();

            Stock stock = CheckStock(quantities,wareHouse);
            stocks.add(stock);
        }

        return stocks.stream()
                .collect(Collectors.toMap(
                        s -> s.getVariant().getSku(),
                        s -> s
                ));
    }

    private void rollbackLocks(
            Map<String, Boolean> lockResults,
            CheckOutProjection route,
            Long userId) {

        lockResults.entrySet().stream()
                .filter(Map.Entry::getValue)
                .forEach(entry -> {
                    String key = "stock_lock:" + entry.getKey() + ":" + route.getStoreId();
                    redisTemplate.opsForHash().delete(key, String.valueOf(userId));
                });
    }


    @Transactional(readOnly = true)
    public CheckOutResponse checkOutOrder(CheckOutRequest request
            ,Map<String,Stock> stockMap
            , CheckOutProjection route
    ) {
        Integer total_quantity = 0;
        BigDecimal shippingFee =BigDecimal.ZERO;
        List<CheckOutProductResponse> checkOutProductResponses = new ArrayList<>();
        Map<String, Integer> skuQuantities = request.getItem().stream()
                .collect(Collectors.toMap(
                        CheckOutItemRequest::getSku,
                        CheckOutItemRequest::getQuantity
                ));
        CheckOutResponse response = new CheckOutResponse();
        for (CheckOutItemRequest checkOutItemRequest : request.getItem()) {
            ProductVariant productVariant = getVariant(checkOutItemRequest.getSku());
            Stock stock = stockMap.get(productVariant.getSku());
            String totalKey = "stock_lock_total:" + route.getStoreId() + ":" + checkOutItemRequest.getSku();
            String value = redisTemplate.opsForValue().get(totalKey);
            Integer lockQuantity = Integer.parseInt(value);
            total_quantity = request.getItem().size();
            skuQuantities.put(checkOutItemRequest.getSku(), checkOutItemRequest.getQuantity());
            BigDecimal fee = shippingCalculator
                    .calculatorShippingFee(skuQuantities,
                            route, total_quantity, route.getDistanceKm(),response);
            response.setFrom("from "+route.getDistanceKm());
            response.setStoreId(route.getStoreId());
            response.setShippingFee(shippingFee.add(fee));
            CheckOutProductResponse checkOutResponse = CheckOutProductResponse.builder()
                    .id(productVariant.getId())
                    .isAvailable(productVariant.getIsAvailable())
                    .quantity(checkOutItemRequest.getQuantity())
                    .sizeName(productVariant.getSize().getName())
                    .colorName(productVariant.getProductColor().getColor().getName())
                    .sku(productVariant.getSku())
                    .productName(productVariant.getProductColor().getProduct().getName()).price(productVariant.getPrice())
                    .finaPrice(productVariant.getPrice().multiply(BigDecimal.valueOf(checkOutItemRequest.getQuantity())))
                    .stock(stock.getSellableQuantity() - lockQuantity)
                    .imageUrl(productVariant.getProductColor().getImages().isEmpty()
                            ? null : productVariant.getProductColor().getImages().get(0).getImageUrl())
                    .build();
            checkOutProductResponses.add(checkOutResponse);

        }

        response.setProducts(checkOutProductResponses);
        BigDecimal sum = response.getProducts().stream().map(CheckOutProductResponse::getFinaPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (request.getVoucherCode() != null) {
            if (!checkSumUtil.verify(request.getVoucherCode().trim())) {
                throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
            }
            sum = CheckVoucher(response, request.getVoucherCode(), sum);
        }

        response.setFinalPrice(sum);
        saveIdempotentResponse(request.getIdempotencyKey(), response);
        return response;
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
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(30));

            log.info("Saved idempotent response for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to save idempotent response for key: {}", key, e);
        }
    }

    private Map<String, Boolean> batchLockStocks(
            List<CheckOutItemRequest> items,
            Map<String, Stock> stocks,
            CheckOutProjection route,
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
            String lockKey = "stock_lock:" + route.getStoreId() + ":" + sku;
            String totalKey = "stock_lock_total:" + route.getStoreId() + ":" + sku;
//            String realKey  = "stock_real:" + route.getStoreId() + ":" + sku;
            Long result = redisTemplate.execute(
                    script,
                    List.of(lockKey, totalKey),
                    String.valueOf(userId),                    // ARGV[1]
                    String.valueOf(item.getQuantity()),        // ARGV[2]
                    String.valueOf(stock.getSellableQuantity()), // ARGV[3]
                    "1800",                                    // ARGV[4]
                    "5"                                        // ARGV[5]
            );

            results.put(sku, result != null && result == 1);
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



