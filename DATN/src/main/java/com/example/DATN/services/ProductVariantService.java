package com.example.DATN.services;

import com.example.DATN.dtos.request.CreationProductVariantRequest;
import com.example.DATN.dtos.request.UpdateProductVariantRequest;
import com.example.DATN.dtos.respone.ProductVariantResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ColorMapper;
import com.example.DATN.mapper.ImageProductMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.mapper.SizeMapper;
import com.example.DATN.models.ProductColor;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Size;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
    private final ProductColorRepository productColorRepository;
    private final SizeRepository sizeRepository;

    @Transactional(rollbackFor = Exception.class)
    public ProductVariantResponse createProductVariant
            (CreationProductVariantRequest request) {
        ProductColor productColor = productColorRepository.findById(request.getProductColorId())
                .orElseThrow(()->new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));

        Size size = sizeRepository.findByName(request.getVariantRequest().getSize().getName())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
        String skugenerate =productColor.getProduct().getProductCode() + "-" + productColor.getColor().getCode() + "-" + size.getCode();
        ProductVariant productVariant = ProductVariant
                .builder()
                .productColor(productColor)
                .stock(request.getVariantRequest().getStock())
                .sku(skugenerate)
                .price(request.getVariantRequest().getPrice())
                .size(size)
                .build();
        ProductVariant savedproductvariant = productVariantRepository.save(productVariant);
        return productVariantMapper.toProductVariantResponse(savedproductvariant);
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


    @Transactional(rollbackFor = Exception.class)
    public ProductVariantResponse updateProductVariant(
            UUID id, UpdateProductVariantRequest request) {
        ProductVariant existingProductVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        boolean skuNeedsUpdate = false;
        if (request.getPrice() != null) {
            existingProductVariant.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            existingProductVariant.setStock(request.getStock());
        }

        if (request.getSize() != null && request.getSize().getName() != null) {
            Size size = sizeRepository.findByName(request.getSize().getName())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
            if(!size.equals(existingProductVariant.getSize())){
                existingProductVariant.setSize(size);
                skuNeedsUpdate = true;
            }
        }


        if(request.getDiscountPrice()!=null){
            existingProductVariant.setDiscountPrice(request.getDiscountPrice());
        }
        if (request.getSku() != null && !request.getSku().isEmpty()) {
            existingProductVariant.setSku(request.getSku());
        }
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
