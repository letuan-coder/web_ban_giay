package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.ProductStatus;
import com.example.DATN.dtos.request.UploadImageRequest;
import com.example.DATN.dtos.request.product.ProductColorRequest;
import com.example.DATN.dtos.request.product.ProductRequest;
import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.respone.PromotionPriceResponse;
import com.example.DATN.dtos.respone.product.*;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.FormatInputString;
import com.example.DATN.mapper.ProductColorMapper;
import com.example.DATN.mapper.ProductMapper;
import com.example.DATN.mapper.ProductVariantIndexMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.example.DATN.repositories.projection.ProductSalesProjection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.DATN.specification.ProductSpecification.filterProducts;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductMapper productMapper;
    private final ProductColorService productColorService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private final RedisTemplate redisTemplate;
    private final String PREFIX = "SHOES";
    private final ImageProductService imageProductService;
    private final FormatInputString formatInputString;
    private final ProductColorMapper productColorMapper;
    private final ImageProductRepository imageProductRepository;
    private final ColorRepository colorRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantIndexMapper productVariantIndexMapper;
    private final ProductReviewRepository productReviewRepository;
    private final ProductVariantMapper productVariantMapper;
    private final ProductVariantIndexRepository productVariantIndexRepository;
    private final ObjectMapper objectMapper;
//    private final ProductRedisRepository productRedisRepository;

    //    public List<ProductResponse> getProductByProductCode(String productCode) {
//        List<Product> ListOfProduct = productRepository.findAllByProductCode(productCode);
//        return ListOfProduct.stream()
//                .map(productMapper::toProductResponse)
//                .collect(Collectors.toList());
//    }

    public List<String> getImageByProductColorId(UUID productColorId) {
        ProductColor productColor = productColorRepository.findById(productColorId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

        return imageProductRepository.findAllByProductColor(productColor)
                .stream()
                .map(ImageProduct::getImageUrl)
                .toList();
    }

    public List<ProductSupplierResponse> getAllProductBySupplier(UUID UUID) {
//        UUID id = java.util.UUID.fromString(UUID);
        Supplier supplier = supplierRepository.findById(UUID)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        List<Product> products = productRepository.findAllBySupplier(supplier);

        return products.stream().map(productMapper::toSupplierDetail)
                .collect(Collectors.toList());
    }

    public Page<ProductSalesProjection> BestSellingProductSales(Pageable pageable) {
        return productRepository.findBestSellingProductsDetail(pageable);

    }

    public List<ProductSalesProjection> WorstSellingProductSales(Pageable page) {
        return productRepository.findWorstSellingProducts(page);
    }

    public static String generate(String prefix, Long index) {
        return prefix + index;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        }
        if (request.getImportPrice().compareTo(request.getPrice()) > 0) {
            throw new ApplicationException(ErrorCode.INVALID_PRICE);
        }
        String formatProductName = formatInputString.formatInputString(request.getName().trim());
        Long snowCode = snowflakeIdGenerator.nextId();
        String productCode = (generate(PREFIX, snowCode));
        String rawDesc = request.getDescription();
        String realDesc = rawDesc.replace("\\n", "\n");
        Product product = productMapper.toProduct(request);
        product.setName(formatProductName);
        product.setDescription(realDesc);
        product.setProductCode(productCode);
        product.setBrand(brand);
        product.setCategory(category);
        product.setSlug(toSlug(formatProductName));
        product.setImportPrice(request.getImportPrice());
        product.setPrice(request.getPrice());
        product.setThumbnailUrl("");
        product.setWeight(request.getWeight());
        product.setHeight(request.getHeight());
        product.setWidth(request.getWidth());
        product.setLength(request.getLength());
        product.setSupplier(supplier);
        Product savedProduct = productRepository.save(product);
        for (String colorCode : request.getColorCodes()) {
            Color color = colorRepository.findByCode(colorCode)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.COLOR_NOT_FOUND));
            ProductVariantRequest productVariantRequest = ProductVariantRequest.builder()
                    .weight(product.getWeight())
                    .Height(product.getHeight())
                    .Length(product.getLength())
                    .Width(product.getWidth())
                    .sizeCodes(request.getSizeCodes())
                    .price(request.getPrice())
                    .stock(null)
                    .build();
            ProductColorRequest productColorRequest = ProductColorRequest.builder()
                    .colorName(color.getName())
                    .productId(product.getId())
                    .variantRequest(productVariantRequest)
                    .build();
            ProductColor productColor = productColorMapper
                    .toEntity(productColorService.createProductColor(productColorRequest));
            ProductVariantIndex index = productVariantIndexMapper.toIndexProduct(product);
            productVariantIndexRepository.save(index);

        }
        UploadImageRequest uploadImageRequest = UploadImageRequest.builder()
                .product(savedProduct)
                .banner(null)
                .imageProduct(null)
                .imageUrl(product.getThumbnailUrl())
                .altText(product.getSlug())
                .file(request.getFile())
                .build();
        imageProductService.uploadImage(uploadImageRequest);
        return productMapper.toProductResponse(savedProduct);

    }

    public ProductVariantDetailResponse getVariantPrice(String sku
            , ProductVariantDetailResponse productResponse)
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
        productResponse.setPrice(response.getOriginalPrice());
        productResponse.setDiscountPrice(response.getDiscountPrice());

        return productResponse;
    }

    public ProductDetailReponse calculateCheckoutPrices(
            ProductDetailReponse productResponse
    ) throws JsonProcessingException {
        List<ProductVariantDetailResponse> productVariantResponseList = new ArrayList<>();
        for (ProductVariantDetailResponse product : productResponse.getVariantDetailResponses()) {
            ProductVariantDetailResponse variantDetailResponse = getVariantPrice(product.getSku(), product);
            productVariantResponseList.add(variantDetailResponse);

        }
        productResponse.setVariantDetailResponses(productVariantResponseList);
        if (!productVariantResponseList.isEmpty()) {
            productResponse.setDiscountPrice(
                    productVariantResponseList.get(productVariantResponseList.size() - 1)
                            .getDiscountPrice()
            );
        }
        return productResponse;
    }

    public List<ProductColorResponse> calculatePromoPrice(
            List<ProductColorResponse> productResponse
    ) throws JsonProcessingException {
        List<ProductVariantResponse> variantResponseList = new ArrayList<>();
        for (ProductColorResponse product : productResponse) {
            List<String> skus = product.getVariantResponses().stream().map(item -> item.getSku()).toList();
            variantResponseList = getPromotionsBySkus(skus, product.getVariantResponses());
            product.setVariantResponses(variantResponseList);
        }

        return productResponse;
    }

    public List<ProductVariantResponse> getPromotionsBySkus(
            List<String> skus, List<ProductVariantResponse> response
    ) {
        List<String> keys = skus.stream()
                .map(sku -> "PROMO:VARIANT:" + sku)
                .toList();

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        Map<String, PromotionPriceResponse> responseMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            Object value = values.get(i);
            if (value == null) continue;

            try {
                PromotionPriceResponse promo =
                        objectMapper.readValue(
                                value.toString(),
                                PromotionPriceResponse.class
                        );
                String sku = skus.get(i);
                responseMap.put(sku, promo);


            } catch (Exception ignored) {
            }
        }
        for (ProductVariantResponse variant : response) {
            PromotionPriceResponse promo = responseMap.get(variant.getSku());
            if (promo != null) {
                variant.setDiscountPrice(promo.getDiscountPrice());
                variant.setPrice(promo.getOriginalPrice());
            }
        }
        return response;
    }

    private String toSlug(String input) {
        if (input == null || input.isBlank()) return "";

        // 1. Thay khoảng trắng bằng dấu gạch ngang
        String nowhitespace = Pattern.compile("\\s+").matcher(input).replaceAll("-");

        // 2. Chuẩn hóa chuỗi (bỏ dấu tiếng Việt)
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");

        // 3. Chuyển về chữ thường
        return slug.toLowerCase(Locale.ROOT);
    }


    public Page<ProductResponse> getAllProducts(
            String productName, Double priceMin, Double priceMax,
            ProductStatus status, Long brandId, Long categoryId,
            String sizeCode, String colorCode, Pageable pageable) {

        Page<Product> productsPage = productRepository.findAll(filterProducts(productName,
                priceMin, priceMax, status, brandId, categoryId, colorCode, sizeCode), pageable);


        return productsPage.map(this::mapProductToProductResponse);

    }

    public List<SearchProductResponse> getProductByProductCode(String productCode) {
        List<Product> products = productRepository.findByProductCode(productCode);
        productVariantIndexRepository.saveAll(products.stream().map(productVariantIndexMapper::toIndexProduct).collect(Collectors.toList()));

        return products.stream().map(productMapper::toSearchDetail)
                .collect(Collectors.toList());
    }

    public List<ProductSupplierResponse> getProductBySupplierId(UUID supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        return productRepository.findBySupplier(supplier)
                .stream().map(productMapper::toSupplierDetail)
                .collect(Collectors.toList());
    }

    //
//    public List<ProductResponse> searchProductsByName(String name) {
//        return productRepository.findByNameContainingIgnoreCase(name).stream()
//                .map(this::mapProductToProductResponse)
//                .collect(Collectors.toList());
//    }

    //    @Async
//    public Long getRealtimeViewByProduct(UUID id)
//            throws JsonProcessingException {
//        String day = DAY_FMT.format(Instant.now());
//        String keyProduct = "product:view:product:" + id + ":" + day;
//        Object value = redisTemplate.opsForValue().get(keyProduct);
//        String view = new ObjectMapper().writeValueAsString(value);
//        long total = value != null ? Long.parseLong(view) : 0L;
//        return total;
//    }
    @Async
    public void SetTotal_View(UUID id) {
        String day = DAY_FMT.format(Instant.now());
        String keyProduct = "product:view:product:" + id + ":" + day;
        redisTemplate.opsForValue().increment(keyProduct, 1L);
    }

    public ProductDetailReponse getProductById(UUID id) throws JsonProcessingException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        productVariantIndexMapper.toIndexProduct(product);
        SetTotal_View(id);
        ProductDetailReponse response = productMapper.toDetail(product);
        return calculateCheckoutPrices(response);

    }

    public ProductResponse getProductAdminById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        return productMapper.toProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

        productVariantIndexRepository.delete(productVariantIndexMapper.toIndexProduct(existingProduct));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));
        existingProduct.setName(formatInputString.formatInputString(request.getName()));
        existingProduct.setDescription(request.getDescription());
        existingProduct.setBrand(brand);
        existingProduct.setCategory(category);
        existingProduct.setSlug(toSlug(request.getName()));
        existingProduct.setUpdatedAt(LocalDateTime.now());
        existingProduct.setPrice(request.getPrice());
        Product updatedProduct = productRepository.save(existingProduct);
        productVariantIndexRepository.save(productVariantIndexMapper.toIndexProduct(updatedProduct));
        return productMapper.toProductResponse(updatedProduct);
    }

    @Transactional
    public ProductResponse updateProductWithImage(UUID id, ProductRequest request, MultipartFile file) {
        ProductResponse updatedProductResponse = updateProduct(id, request);
        Product productWithImage = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        UploadImageRequest uploadImageRequest = UploadImageRequest.builder()
                .file(file)
                .product(productWithImage)
                .imageProduct(null)
                .banner(null)
                .altText(updatedProductResponse.getSlug())
                .build();
        imageProductService.uploadImage(uploadImageRequest);
        return productMapper.toProductResponse(productWithImage);
    }


    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductColor> productColor = productColorRepository.findAllByProduct(product);
        imageProductService.deleteImage(product.getId());
        for (ProductColor pc : productColor) {
            productColorService.deleteProductColor(pc.getId());

        }
        productVariantIndexRepository.delete(productVariantIndexMapper.toIndexProduct(product));
        productRepository.delete(product);
    }

    public ProductColorResponse getCheapestColor(
            List<ProductColorResponse> colors
    ) {
        ProductColorResponse cheapestColor = null;
        BigDecimal minPrice = null;

        for (ProductColorResponse color : colors) {
            for (ProductVariantResponse v : color.getVariantResponses()) {

                BigDecimal currentPrice =
                        v.getDiscountPrice() != null
                                ? v.getDiscountPrice()
                                : v.getPrice();

                if (minPrice == null || currentPrice.compareTo(minPrice) < 0) {
                    minPrice = currentPrice;
                    cheapestColor = color;
                }
            }
        }

        return cheapestColor;
    }

    public BigDecimal calculateMinProductPrice(ProductColorResponse product) {

        return
        product.getVariantResponses().stream().filter(Objects::nonNull)
                .map(variantResponse -> variantResponse.getDiscountPrice()!=null
                        ?variantResponse.getDiscountPrice():variantResponse.getPrice())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @SneakyThrows
    private ProductResponse mapProductToProductResponse(Product product) {
        List<ProductReview> productReviews = productReviewRepository.findAllByProduct(product);
        List<ProductColorResponse> colorResponse = productColorMapper.toProductColorResponses(product.getProductColors());
        colorResponse = calculatePromoPrice(colorResponse);
        ProductColorResponse cheapestColor = getCheapestColor(colorResponse);
        BigDecimal discountPrice =
                calculateMinProductPrice(cheapestColor);
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .productCode(product.getProductCode())
                .available(product.getAvailable())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .ThumbnailUrl(product.getThumbnailUrl())
                .brandId(product.getBrand().getId())
                .categoryId(product.getCategory().getId())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .colorResponses(colorResponse)
                .altText(product.getAltText())
                .price(product.getPrice())
                .discountPrice(discountPrice)
                .averageRating(productMapper.calculateAverageRating(productReviews))
                .totalComment(productReviews.size())
                .totalView(product.getTotalView())
                .build();
    }
}
