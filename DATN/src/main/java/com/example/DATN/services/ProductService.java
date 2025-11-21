package com.example.DATN.services;

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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
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
    private final ProductColorService productColorService;
    private final String PREFIX = "SHOES_";
    private final ImageProductService imageProductService;
    private final FormatInputString formatInputString;


    public List<ProductResponse> getProductByProductCode(String productCode) {
        List<Product> ListOfProduct = productRepository.findAllByProductCode(productCode);
        return ListOfProduct.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    private String generateProductCode() {
        return UUID.randomUUID().toString().substring(0, 5).toUpperCase();
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

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<ProductResponse> responses = productRepository.findAll(pageable)
                .map(productMapper::toProductResponse);

        return responses;
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

        existingProduct.setName(formatInputString.formatInputString(request.getName()));
        existingProduct.setDescription(request.getDescription());
        existingProduct.setBrand(brand);
        existingProduct.setCategory(category);
        existingProduct.setSlug(toSlug(request.getName()));
        existingProduct.setUpdatedAt(LocalDateTime.now());
        existingProduct.setWeight(request.getWeight());
        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
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

    @Transactional
    public void addProductsFromExcel(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header row
            }

            List<ProductRequest> productRequests = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                ProductRequest request = new ProductRequest();

                Cell nameCell = row.getCell(0);
                if (nameCell != null) {
                    request.setName(nameCell.getStringCellValue());
                }

                Cell descriptionCell = row.getCell(1);
                if (descriptionCell != null) {
                    request.setDescription(descriptionCell.getStringCellValue());
                }

                Cell brandIdCell = row.getCell(2);
                if (brandIdCell != null) {
                    request.setBrandId((long) brandIdCell.getNumericCellValue());
                }

                Cell categoryIdCell = row.getCell(3);
                if (categoryIdCell != null) {
                    request.setCategoryId((long) categoryIdCell.getNumericCellValue());
                }
                productRequests.add(request);
            }

            for (ProductRequest productRequest : productRequests) {
                createProduct(productRequest);
            }

        } catch (IOException e) {
            throw new ApplicationException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}