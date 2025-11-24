package com.example.DATN.services;

import com.example.DATN.constant.ProductStatus;
import com.example.DATN.dtos.request.product.ProductRequest;
import com.example.DATN.dtos.respone.product.ProductResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.FormatInputString;
import com.example.DATN.mapper.ProductMapper;
import com.example.DATN.models.Brand;
import com.example.DATN.models.Category;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductColor;
import com.example.DATN.repositories.BrandRepository;
import com.example.DATN.repositories.CategoryRepository;
import com.example.DATN.repositories.ProductColorRepository;
import com.example.DATN.repositories.ProductRepository;
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
    private final String PREFIX = "SHOES_";
    private final ImageProductService imageProductService;
    private final FormatInputString formatInputString;
//
//    public List<ProductResponse> getProductByProductCode(String productCode) {
//        List<Product> ListOfProduct = productRepository.findAllByProductCode(productCode);
//        return ListOfProduct.stream()
//                .map(productMapper::toProductResponse)
//                .collect(Collectors.toList());
//    }

    private String generateProductCode() {
        return UUID.randomUUID().toString().substring(0, 3).toUpperCase();
    }

    public static String generate(String prefix, String index) {
        return prefix + index;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));

        String formatProductName = formatInputString.formatInputString(request.getName().trim());
        String productCode = (generate(PREFIX, generateProductCode()));
        String formatDescription = formatInputString.formatInputString(request.getDescription().trim());
        Product product = productMapper.toProduct(request);
        product.setName(formatProductName);
        product.setDescription(formatDescription);
        product.setProductCode(productCode);
        product.setBrand(brand);
        product.setCategory(category);
        product.setSlug(toSlug(formatProductName));
        product.setWeight(request.getWeight());
        product.setPrice(request.getPrice());
        product.setThumbnailUrl("");
        Product savedProduct = productRepository.save(product);
        imageProductService.uploadThumbnailImages(savedProduct.getId(), request.getFile());
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
    public void addProductsFromExcel(MultipartFile file) {
        // Placeholder for excel import logic
    }

    public Page<ProductResponse> getAllProducts(
            String productName, Double priceMin, Double priceMax,
            ProductStatus status,Long brandId,Long categoryId,Integer size,
            String color,Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(filterProducts(productName,
                priceMin, priceMax, status,brandId,categoryId,size,color),pageable);
        return productsPage.map(this::mapProductToProductResponse);
    }

    public List<ProductResponse> getProductByProductCode(String productCode) {
        return productRepository.findByProductCode(productCode).stream()
                .map(this::mapProductToProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapProductToProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(UUID id) {
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
        existingProduct.setWeight(request.getWeight());
        existingProduct.setPrice(request.getPrice());

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    @Transactional
    public ProductResponse updateProductWithImage(UUID id, ProductRequest request, MultipartFile file) {
        ProductResponse updatedProductResponse = updateProduct(id, request);

        imageProductService.uploadThumbnailImages(id, file);

        Product productWithImage = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toProductResponse(productWithImage);
    }


    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductColor> productColor = productColorRepository.findAllByProduct(product);
        for (ProductColor pc : productColor) {
            productColorService.deleteProductColor(pc.getId());

        }
        productRepository.delete(product);
    }

    private ProductResponse mapProductToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .productCode(product.getProductCode())
                .available(product.getAvailable())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .weight(product.getWeight())
                .ThumbnailUrl(product.getThumbnailUrl())
                .altText(product.getAltText())
                .price(product.getPrice())
                .build();
    }
}
