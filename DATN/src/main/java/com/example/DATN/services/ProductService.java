package com.example.DATN.services;

import com.example.DATN.constant.Util.SkuUtil;
import com.example.DATN.dtos.request.ProductRequest;
import com.example.DATN.dtos.respone.ProductResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ProductMapper;
import com.example.DATN.models.Brand;
import com.example.DATN.models.Category;
import com.example.DATN.models.Product;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductMapper productMapper;
    private final SkuUtil skuConstant;

    private final String PREFIX = "SHOES";
    public static String generate(String prefix, long index) {
        return prefix + String.format("%03d", index);
    }
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String productCode =generate(PREFIX, productRepository.count()+1);
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));
        String formatProductName = request.getName().trim();
        Product product = productMapper.toProduct(request);
        product.setName(formatProductName);
        product.setDescription(request.getDescription());
        product.setProductCode(productCode);
        product.setBrand(brand);
        product.setCategory(category);
        product.setSlug(toSlug(formatProductName));
        Product savedProduct = productRepository.save(product);

        return productMapper.toProductResponse(savedProduct);

    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toProductResponse);
    }

    public List<ProductResponse> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(productMapper::toProductResponse)
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

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setBrand(brand);
        existingProduct.setCategory(category);
        existingProduct.setSlug(toSlug(request.getName()));
        existingProduct.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
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
}