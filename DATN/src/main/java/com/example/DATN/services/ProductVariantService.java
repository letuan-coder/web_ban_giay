package com.example.DATN.services;

import com.example.DATN.dtos.request.CreationProductVariantRequest;
import com.example.DATN.dtos.request.ProductVariantRequest;
import com.example.DATN.dtos.respone.ImageProductResponse;
import com.example.DATN.dtos.respone.ProductVariantResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ColorMapper;
import com.example.DATN.mapper.ImageProductMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.mapper.SizeMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantService {
    //REPOSITORIES
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ImageProductRepository imageProductRepository;

    //MAPPERS
    private final ProductVariantMapper productVariantMapper;
    private final SizeMapper sizeMapper;
    private final ColorMapper colorMapper;
    private final ImageProductMapper imageProductMapper;
    //SERVICES
    private final ProductService productService;
    private final ImageProductService imageProductService;
    private final ColorService colorService;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;


    @Transactional(rollbackFor = Exception.class)
    public ProductVariantResponse createProductVariant
            (CreationProductVariantRequest request) {
        Color color = new Color();
        if (request.getVariantRequest().getColors()!=null){
           var colordto =
                    colorService.createColor
                            (request.getVariantRequest().getColors());
           color = colorMapper.toEntity(colordto);
           colorRepository.save(color);
        }
        var productResponse = productService.createProduct(request.getProductRequest());
        var productId = productResponse.getId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        if (request.getVariantRequest().getColorName()!=null) {
            color = colorRepository.findByName(request.getVariantRequest().getColorName())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.COLOR_NOT_FOUND));
        }
        Size size = sizeRepository.findByName(request.getVariantRequest().getSize().getName())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
        String skugenerate = product.getProductCode() + "-" + color.getCode() + "-" + size.getCode();
        ProductVariant productVariant = ProductVariant
                .builder()
                .product(product)
                .stock(request.getVariantRequest().getStock())
                .sku(skugenerate)
                .price(request.getVariantRequest().getPrice())
                .size(size)
                .color(color)
                .build();
        ProductVariant savedproductvariant = productVariantRepository.save(productVariant);
        List<ImageProduct> imageEntities = new ArrayList<>();
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            var images = imageProductService.uploadImages(productVariant.getId(), request.getFiles(), request.getAltText());
            for (ImageProductResponse response : images) {
                ImageProduct image = imageProductMapper.toEntity(response);
                imageProductRepository.save(image);
                imageEntities.add(image);
            }
            savedproductvariant.setImages(imageEntities);
            productVariantRepository.save(savedproductvariant);
        }
        return productVariantMapper.toProductVariantResponse(savedproductvariant);
    }

    public List<ProductVariantResponse> getProductVariantsByProductId(UUID productId) {
        List<ProductVariant> productVariants = productVariantRepository.findByProductId(productId);
        return productVariants.stream()
                .map(productVariantMapper::toProductVariantResponse)
                .collect(Collectors.toList());
    }

    public ProductVariantResponse getProductVariantById(UUID id) {
        ProductVariant productVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        return productVariantMapper.toProductVariantResponse(productVariant);
    }

    public List<ProductVariantResponse> getallproductvariant() {

        return productVariantRepository.findAll()
                .stream()
                .map(productVariantMapper::toProductVariantResponse)
                .toList();
    }


    @Transactional
    public ProductVariantResponse updateProductVariant(UUID id, ProductVariantRequest request, UUID productId) {
        ProductVariant existingProductVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

        existingProductVariant.setProduct(product);
        existingProductVariant.setPrice(request.getPrice());
        existingProductVariant.setStock(request.getStock());
        ProductVariant updatedProductVariant = productVariantRepository.save(existingProductVariant);
        return productVariantMapper.toProductVariantResponse(updatedProductVariant);
    }

    @Transactional
    public void deleteProductVariant(UUID id) {
        ProductVariant productVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        productVariantRepository.delete(productVariant);
    }
}
