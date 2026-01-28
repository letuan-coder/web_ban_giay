package com.example.DATN.services;


import com.example.DATN.constant.Is_Available;
import com.example.DATN.constant.VariantType;
import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.request.product.UpdateProductVariantRequest;
import com.example.DATN.dtos.respone.PromotionPriceResponse;
import com.example.DATN.dtos.respone.product.ProductVariantResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantMapper productVariantMapper;
    private final ObjectMapper objectMapper;
    private final ProductColorRepository productColorRepository;
    private final SizeRepository sizeRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private final ProductViewRepository productViewRepository;
    private final RedisTemplate redisTemplate;
    private final ProductViewAggregateRepository productViewAggregateRepository;
    private final ProductRepository productRepository;

    @Transactional(rollbackFor = Exception.class)
    public List<ProductVariantResponse> createListProductVariant
            (UUID productcolorId,
             ProductVariantRequest request) {
        ProductColor productColor = productColorRepository.findById(productcolorId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));


        List<ProductVariant> productVariants = new ArrayList<>();
//        for (ProductVariantRequest request : requests) {
        for (String sizeRequest : request.getSizeCodes()) {
            Size size = sizeRepository.findByCode(sizeRequest).orElseThrow(() ->
                    new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
            String skugenerate = productColor.getProduct().getProductCode() + "-" +
                    productColor.getColor().getCode() + "-" + size.getCode();
            ProductVariant productVariant = ProductVariant
                    .builder()
                    .weight(request.getWeight())
                    .height(request.getWidth())
                    .length(request.getLength())
                    .width(request.getWidth())
                    .productColor(productColor)
                    .stocks(null)
                    .size(size)
                    .sku(skugenerate)
                    .isAvailable(Is_Available.NOT_AVAILABLE)
                    .price(productColor.getProduct().getPrice())
                    .build();
            productVariants.add(productVariant);
            productVariantRepository.save(productVariant);

        }
        productVariantRepository.saveAll(productVariants);
        List<ProductVariant> savedProductVariants = productVariantRepository.saveAll(productVariants);
        return savedProductVariants.stream()
                .map(productVariantMapper::toProductVariantResponse)
                .collect(Collectors.toList());
    }

    public ProductVariantResponse getVariantPrice(String sku, ProductVariantResponse variantResponse)
            throws JsonProcessingException {

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
        variantResponse.setPrice(response.getOriginalPrice());
        variantResponse.setDiscountPrice(response.getDiscountPrice());

        return variantResponse;
    }

    public List<ProductVariantResponse> calculateCheckoutPrices(
            List<ProductVariantResponse> response,
            List<ProductVariant> variants
    ) throws JsonProcessingException {
        List<ProductVariantResponse> productVariantResponseList = new ArrayList<>();

        for (ProductVariant variant : variants) {
            String key = variant.getSku();
            ProductVariantResponse variantRes = productVariantMapper.toProductVariantResponse(variant);
            ProductVariantResponse variantResponse = getVariantPrice(key, variantRes);
            productVariantResponseList.add(variantResponse);
        }
        return  productVariantResponseList;
    }

    @Async
    public void AddView(ProductVariant variant, Authentication authentication) {
        User user = null;
        UUID productId = variant.getProductColor().getProduct().getId();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            String username = authentication.getName();
            user = userRepository.findByUsername(username).orElse(null);
            ProductView view = ProductView.builder()
                    .variant(variant)
                    .variantType(VariantType.VIEW)
                    .user(user)
                    .build();
            productViewRepository.save(view);
        } else {
            ProductView view = ProductView.builder()
                    .variant(variant)
                    .variantType(VariantType.VIEW)
                    .anonymousId(authentication.getName())
                    .build();
            productViewRepository.save(view);

        }
        increaseView(variant.getId(), productId);
    }

    @Scheduled(fixedRate = 300000)
    public void refreshVariantAvailability() {
        List<ProductVariant> variants = productVariantRepository.findAll();

    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void syncViewsToDB() {
        Set<String> keys = redisTemplate.keys("product:view:*");
        for (String key : keys) {
            Long views = ((Number) redisTemplate.opsForValue().get(key)).longValue();
            UUID productId = extractProductIdFromKey(key);
            productRepository.incrementTotalView(productId, views);
            System.out.println("Updating product " + productId + " with views " + views);
            redisTemplate.delete(key);
            System.out.println("Cron chạy lúc: " + LocalDateTime.now());

        }
    }

    private UUID extractProductIdFromKey(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 4) {
            try {
                return UUID.fromString(parts[3]); // chỉ parse nếu đúng UUID
            } catch (IllegalArgumentException e) {
                System.err.println("Bỏ qua key không hợp lệ: " + key);
                return null; // hoặc throw custom, tùy cách xử lý
            }
        }
        System.err.println("Bỏ qua key format sai: " + key);
        return null;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void syncViewsDailyCron() {
        Set<String> keys = redisTemplate.keys("product:view:*");
        Instant now = Instant.now();
        for (String key : keys) {
            UUID productId = extractProductIdFromKey(key);
            if (productId == null) continue;
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) continue;
            Long views = ((Number) value).longValue();
            productRepository.incrementTotalView(productId, views);
            UUID variantId = productId;
            Instant timeBucket = now.truncatedTo(ChronoUnit.DAYS);
            Optional<ProductViewAggregate> opt = productViewAggregateRepository
                    .findByVariantIdAndProductIdAndTimeBucket(productId, variantId, timeBucket);
            if (opt.isPresent()) {
                ProductViewAggregate aggregate = opt.get();
                aggregate.setTotalView(aggregate.getTotalView() + views.intValue());
                productViewAggregateRepository.save(aggregate);
            } else {
                ProductViewAggregate aggregate = new ProductViewAggregate();
                aggregate.setProductId(productId);
                aggregate.setVariantId(variantId);
                aggregate.setTimeBucket(timeBucket);
                aggregate.setTotalView(views.intValue());
                productViewAggregateRepository.save(aggregate);
            }

            redisTemplate.delete(key);
        }
    }

    public void increaseView(UUID variantId, UUID product) {
        String day = DAY_FMT.format(Instant.now());
        String key = "product:view:variant:" + variantId + ":" + day;

        String keyProduct = "product:view:product:" + product + ":" + day;
        redisTemplate.opsForValue().increment(keyProduct, 1);
        redisTemplate.expire(keyProduct, Duration.ofDays(2));
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, Duration.ofDays(2));
    }

    public ProductVariantResponse getProductVariantById(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ProductVariant productVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        AddView(productVariant, auth);
        ProductVariantResponse response =
                productVariantMapper.toProductVariantResponse(productVariant);
        return response;
    }

    public ProductVariantResponse getProductVariantBySKU(String sku) {
        ProductVariant productVariant = productVariantRepository.findBysku(sku)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        return productVariantMapper.toProductVariantResponse(productVariant);
    }


    public List<ProductVariantResponse> getallproductvariant() throws JsonProcessingException {
        List<ProductVariant> variants = productVariantRepository.findAll();
        List<ProductVariantResponse> responseList = variants
                .stream()
                .map(productVariantMapper::toProductVariantResponse)
                .toList();
        return calculateCheckoutPrices(responseList, variants);

    }

    public List<ProductVariantResponse> UpdateProductVariantById
            (UUID variantId, UpdateProductVariantRequest request) {
        ProductVariant productVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        if (request.getPrice() != null) {
            productVariant.setPrice(request.getPrice());
        }

        if (request.getSku() != null && !request.getSku().isEmpty()) {
            productVariant.setSku(request.getSku());
        }
        productVariantRepository.save(productVariant);
        return productVariantMapper.toProductVariantResponse(productVariantRepository.findAllByproductColor(productVariant.getProductColor()));
    }

    @Transactional(rollbackFor = Exception.class)
    public List<ProductVariantResponse> updateProductVariant(
            UUID id, List<UpdateProductVariantRequest> requests) {
        ProductColor productColor = productColorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductVariant> ListexistingProductVariant = productVariantRepository.findAllByproductColor(productColor);
        List<ProductVariantResponse> updatedResponses = new ArrayList<>();
        for (ProductVariant existingProductVariant : ListexistingProductVariant) {
            Optional<UpdateProductVariantRequest> optionalMatch = requests.stream()
                    .filter(v -> v.getId().equals(existingProductVariant.getId()))
                    .findFirst();
            if (optionalMatch.isEmpty()) continue;
            UpdateProductVariantRequest match = optionalMatch.get();
            boolean skuNeedsUpdate = false;

            if (match.getPrice() != null) {
                existingProductVariant.setPrice(match.getPrice());
            }
            Size size = existingProductVariant.getSize();

            if (match.getSize() != null && match.getSize().getName() != null) {
                size = sizeRepository.findByName(existingProductVariant.getSize().getName())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
                if (!size.equals(match.getSize())) {
                    existingProductVariant.setSize(size);
                    skuNeedsUpdate = true;
                }
            }

            if (match.getSku() != null && !match.getSku().isEmpty()) {
                existingProductVariant.setSku(match.getSku());
            }
            ProductVariant updatedProductVariant = productVariantRepository.save(existingProductVariant);
            updatedResponses.add(productVariantMapper.toProductVariantResponse(updatedProductVariant));

        }
        return updatedResponses;
    }

    @Transactional
    public void deleteProductVariant(UUID id) {
        ProductVariant productVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        productVariantRepository.delete(productVariant);
    }

    @Transactional
    public void deleteProductColor(UUID id) {
        ProductColor productColor = productColorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        productColorRepository.delete(productColor);
    }

    @Scheduled(fixedRate = 300000) // 5 phút
    @Transactional
    public void refreshAvailability() {
        productVariantRepository.setNotAvailableIfOutOfStockNative();
        productVariantRepository.setAvailableIfInStockNative();
    }

    @Cacheable
            (value = "variantAvailability", key = "#variantId")
    public boolean isAvailable(UUID variantId) {
        // Nếu cache không có, query DB
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        return variant.getIsAvailable() == Is_Available.AVAILABLE;
    }

    @CacheEvict(value = "variantAvailability", key = "#variantId")
    public void updateVariantAvailability(UUID variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        productVariantRepository.save(variant);
    }


}
