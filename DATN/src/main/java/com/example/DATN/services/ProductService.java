package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.ProductStatus;
import com.example.DATN.dtos.request.UploadImageRequest;
import com.example.DATN.dtos.request.product.ProductColorRequest;
import com.example.DATN.dtos.request.product.ProductRequest;
import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.respone.product.ProductDetailReponse;
import com.example.DATN.dtos.respone.product.ProductResponse;
import com.example.DATN.dtos.respone.product.ProductSupplierResponse;
import com.example.DATN.dtos.respone.product.SearchProductResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.FormatInputString;
import com.example.DATN.mapper.ProductColorMapper;
import com.example.DATN.mapper.ProductMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
    private final String PREFIX = "SHOES";
    private final ImageProductService imageProductService;
    private final FormatInputString formatInputString;
    private final ProductColorMapper productColorMapper;
    private final ImageProductRepository imageProductRepository;
    private final ColorRepository colorRepository;
    private final SupplierRepository supplierRepository;
    private final ProductReviewRepository productReviewRepository;
//    private final ProductRedisRepository productRedisRepository;

    //    public List<ProductResponse> getProductByProductCode(String productCode) {
//        List<Product> ListOfProduct = productRepository.findAllByProductCode(productCode);
//        return ListOfProduct.stream()
//                .map(productMapper::toProductResponse)
//                .collect(Collectors.toList());
//    }
    public List<String> getImageByProductColorId(UUID productColorId){
            ProductColor productColor = productColorRepository.findById(productColorId)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

            return imageProductRepository.findAllByProductColor(productColor)
                    .stream()
                    .map(ImageProduct::getImageUrl)
                    .toList();
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
        Supplier supplier =null;
        if(request.getSupplierId()!=null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        }
        if (request.getImportPrice().compareTo(request.getPrice()) > 0) {
            throw new ApplicationException(ErrorCode.INVALID_PRICE);
        }
        String formatProductName = formatInputString.formatInputString(request.getName().trim());
        Long snowCode= snowflakeIdGenerator.nextId();;
        String productCode = (generate(PREFIX, snowCode));
//        String formatDescription = formatInputString.formatInputString(request.getDescription());
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
        product.setSupplier(supplier);
        Product savedProduct = productRepository.save(product);
        for(String colorCode :request.getColorCodes()) {
            Color color = colorRepository.findByCode(colorCode)
                    .orElseThrow(()->new ApplicationException(ErrorCode.COLOR_NOT_FOUND));
            ProductVariantRequest productVariantRequest = ProductVariantRequest.builder()
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
//            ProductRedis productRedis = ProductRedis.builder()
//                    .id(savedProduct.getId())
//                    .name(savedProduct.getName())
//                    .productCode(savedProduct.getProductCode())
//                    .price(savedProduct.getPrice())
//                    .thumbnailUrl(savedProduct.getThumbnailUrl())
//                    .build();
//            productRedisRepository.save(productRedis);
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
            ProductStatus status,Long brandId,Long categoryId, String sizeCode,String colorCode,Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(filterProducts(productName,
                priceMin, priceMax, status,brandId,categoryId,colorCode,sizeCode),pageable);
        return productsPage.map(this::mapProductToProductResponse);
    }

    public List<SearchProductResponse> getProductByProductCode(String productCode) {
        return productRepository.findByProductCode(productCode)
                .stream().map(productMapper::toSearchDetail)
                .collect(Collectors.toList());
    }
    public List<ProductSupplierResponse> getProductBySupplierId(UUID supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        return productRepository.findBySupplier(supplier)
                .stream().map(productMapper::toSupplierDetail)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapProductToProductResponse)
                .collect(Collectors.toList());
    }

    public ProductDetailReponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        return productMapper.toDetail(product);
    }

    public ProductResponse getProductAdminById(UUID id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        return productMapper.toProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

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
        productRepository.delete(product);
    }

    private ProductResponse mapProductToProductResponse(Product product) {
        List<ProductReview> productReviews = productReviewRepository.findAllByProduct(product);
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
                .colorResponses(productColorMapper.toProductColorResponses(product.getProductColors()))
                .altText(product.getAltText())
                .price(product.getPrice())
                .averageRating(productMapper.calculateAverageRating(productReviews))
                .build();
    }
}
