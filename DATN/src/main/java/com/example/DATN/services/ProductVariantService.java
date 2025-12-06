package com.example.DATN.services;


import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.request.product.UpdateProductVariantRequest;
import com.example.DATN.dtos.respone.product.ProductVariantResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;

import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.ProductColorRepository;
//import com.example.DATN.mapper.ProductVariantIndexMapper;
//import com.example.DATN.repositories.ProductVariantIndexRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantMapper productVariantMapper;
//    private final ProductVariantIndexRepository productVariantIndexRepository;
//    private final ProductVariantIndexMapper productVariantIndexMapper;
    private final ProductColorRepository productColorRepository;
    private final SizeRepository sizeRepository;
    private final StoreService storeService;

    @Transactional(rollbackFor = Exception.class)
    public List<ProductVariantResponse> createListProductVariant
            (UUID productcolorId,
             ProductVariantRequest request)
    {
        ProductColor productColor = productColorRepository.findById(productcolorId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        Set<Store> stores = storeService.getAllStore();
        Set<Stock> stock = new HashSet<>();

        List<ProductVariant> productVariants = new ArrayList<>();
//        for (ProductVariantRequest request : requests) {
            for (String sizeRequest : request.getSizeCodes()) {
                Size size = sizeRepository.findByCode(sizeRequest).orElseThrow(() ->
                        new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
                String skugenerate = productColor.getProduct().getProductCode() + "-" +
                        productColor.getColor().getCode() + "-" + size.getCode();
                ProductVariant productVariant = ProductVariant
                        .builder()
                        .productColor(productColor)
                        .stocks(null)
                        .size(size)
                        .sku(skugenerate)
                        .price(productColor.getProduct().getPrice())
                        .build();
                productVariants.add(productVariant);
                productVariantRepository.save(productVariant);
//                for (Store response:stores) {
//                    Stock stockForStore = Stock.builder()
//                            .store(response)
//                            .quantity(0)
//                            .minQuantity(0)
//                            .build();
//                    stock.add(stockForStore);
//                }
//                ProductVariantIndex index = productVariantIndexMapper.toIndex(productVariant);
//                productVariantIndexRepository.save(index);
            }
            productVariantRepository.saveAll(productVariants);
        List<ProductVariant> savedProductVariants = productVariantRepository.saveAll(productVariants);
        return savedProductVariants.stream()
                .map(productVariantMapper::toProductVariantResponse)
                .collect(Collectors.toList());
    }


    public ProductVariantResponse getProductVariantById(UUID id) {
        ProductVariant productVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        return productVariantMapper.toProductVariantResponse(productVariant);
    }
    public ProductVariantResponse getProductVariantBySKU(String sku) {
        ProductVariant productVariant = productVariantRepository.findBysku(sku)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        return productVariantMapper.toProductVariantResponse(productVariant);
    }


    public List<ProductVariantResponse> getallproductvariant() {
        List<ProductVariant> responses = productVariantRepository.findAll();
        return responses
                .stream()
                .map(productVariantMapper::toProductVariantResponse)
                .toList();
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


}
