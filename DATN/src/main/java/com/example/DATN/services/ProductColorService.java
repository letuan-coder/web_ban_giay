package com.example.DATN.services;

import com.example.DATN.dtos.request.CreationProductVariantRequest;
import com.example.DATN.dtos.request.ProductColorRequest;
import com.example.DATN.dtos.request.ProductVariantRequest;
import com.example.DATN.dtos.request.UpdateProductVariantRequest;
import com.example.DATN.dtos.respone.ColorResponse;
import com.example.DATN.dtos.respone.ProductColorResponse;
import com.example.DATN.dtos.respone.ProductVariantResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ColorMapper;
import com.example.DATN.mapper.ProductColorMapper;
import com.example.DATN.mapper.ProductMapper;
import com.example.DATN.mapper.ImageProductMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.Color;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductColor;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductColorService {
    private final ProductColorRepository productColorRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final ColorMapper colorMapper;
    private final ProductColorMapper productColorMapper;
    private final ProductVariantService productVariantService;
    private final ImageProductService imageProductService;
    private final ProductVariantMapper productVariantMapper;
    private final ProductMapper productMapper;
    private final ImageProductMapper imageProductMapper;
    private final ColorService colorService;
    private final ProductVariantRepository productVariantRepository;
    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    @Transactional(rollbackOn = Exception.class)
    public ProductColorResponse createProductColor(ProductColorRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        Color color = null;
        if (request.getColorName() != null) {
            String colorName = request.getColorName();
            color = colorRepository.findByName(colorName)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.COLOR_NOT_FOUND));

        }
        if (request.getColor() != null) {
            ColorResponse response = colorService.createColor(request.getColor());
            color = colorRepository.findByName(response.getName())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        }

        if (productColorRepository.existsByProductAndColor(product, color)) {
            throw new ApplicationException(ErrorCode.PRODUCT_COLOR_EXISTED);
        }
        List<ProductVariantRequest> requests = request.getVariantRequests();
        ProductColor productColor = ProductColor.builder()
                .product(product)
                .color(color)
                .build();
        ProductColor savedProductColor = productColorRepository.save(productColor);
        List<ProductVariantResponse> productVariantResponses = request.getVariantRequests().stream()
                .map(variantRequest -> {
                    CreationProductVariantRequest creationRequest =
                            CreationProductVariantRequest.builder()
                                    .productColorId(savedProductColor.getId())
                                    .variantRequest(variantRequest)
                                    .build();
                    return productVariantService.createProductVariant(creationRequest);
                })
                .collect(Collectors.toList());

        List<UUID> variantIds = productVariantResponses.stream()
                .map(ProductVariantResponse::getId)
                .collect(Collectors.toList());
        List<ProductVariant> savedVariants = productVariantRepository.findAllById(variantIds);
        savedProductColor.setVariants(savedVariants);

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            savedProductColor.setImages(imageProductService.uploadImages
                    (savedProductColor.getId(), savedProductColor.getProduct().getId(), request.getFiles(), request.getAltText()));
        }

//        ProductColorResponse response = ProductColorResponse.builder()
//                .id(savedProductColor.getId())
////                .productResponse(productMapper.toProductResponse(savedProductColor.getProduct()))
//                .color(colorMapper.toColorResponse(savedProductColor.getColor()))
//                .variantResponses(productVariantMapper.toProductVariantResponse(savedProductColor.getVariants()))
//                .images(imageProductMapper.toImageProductResponses(savedProductColor.getImages()))
//                .build();
        ProductColorResponse response= productColorMapper.toProductColorResponse(savedProductColor);
        response.setVariantResponses(productVariantMapper.toProductVariantResponse(savedProductColor.getVariants()));
        return response;

    }

    public ProductColorResponse getProductColorById(UUID id) {
        ProductColor productColor = productColorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        return productColorMapper.toProductColorResponse(productColor);
    }

    public List<ProductColorResponse> getAllProductColors() {
        return productColorRepository.findAll().stream()
                .map(productColorMapper::toProductColorResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductColorResponse updateProductColor(UUID id, ProductColorRequest request) {
        ProductColor existingProductColor = productColorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));

        if (request.getVariantRequests() != null && !request.getVariantRequests().isEmpty()) {
            request.getVariantRequests().forEach(variantRequest -> {
                if (variantRequest.getId() != null) {
                    UpdateProductVariantRequest updateRequest = UpdateProductVariantRequest.builder()
                            .size(variantRequest.getSize())
                            .price(variantRequest.getPrice())
                            .stock(variantRequest.getStock())
                            .build();
                    productVariantService.updateProductVariant(variantRequest.getId(), updateRequest);
                } else {
                    CreationProductVariantRequest creationRequest = new CreationProductVariantRequest(existingProductColor.getProduct().getId(), variantRequest);
                    productVariantService.createProductVariant(creationRequest);
                }
            });
        }

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            imageProductService.uploadImages(existingProductColor.getId(), existingProductColor.getProduct().getId(), request.getFiles(), request.getAltText());
        }

        return productColorMapper.toProductColorResponse(existingProductColor);
    }

    @Transactional
    public void deleteProductColor(UUID id) {
        ProductColor productColor = productColorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        productColorRepository.delete(productColor);
    }
}