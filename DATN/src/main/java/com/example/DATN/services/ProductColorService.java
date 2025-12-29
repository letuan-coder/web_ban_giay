package com.example.DATN.services;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.constant.Util.FileUtil;
import com.example.DATN.dtos.request.UploadImageRequest;
import com.example.DATN.dtos.request.product.ProductColorRequest;
import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.request.product.UpdateProductColorRequest;
import com.example.DATN.dtos.request.product.UpdateProductVariantRequest;
import com.example.DATN.dtos.respone.ColorResponse;
import com.example.DATN.dtos.respone.product.ImageProductResponse;
import com.example.DATN.dtos.respone.product.ProductColorResponse;
import com.example.DATN.dtos.respone.product.ProductVariantResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ImageProductMapper;
import com.example.DATN.mapper.ProductColorMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductColorService {
    private final ProductColorRepository productColorRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final ProductColorMapper productColorMapper;
    private final ProductVariantService productVariantService;
    private final ImageProductService imageProductService;
    private final ProductVariantMapper productVariantMapper;
    private final ImageProductMapper imageProductMapper;
    private final ColorService colorService;
    private final ProductVariantRepository productVariantRepository;

    private final PromotionRepository promotionRepository;
    private final ImageProductRepository imageProductRepository;

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
        ProductColor productColor = ProductColor.builder()
                .product(product)
                .color(color)
                .build();
        ProductColor savedProductColor = productColorRepository.save(productColor);
        List<ProductVariantResponse> productVariantResponses = productVariantService
                .createListProductVariant(savedProductColor.getId(),request.getVariantRequest());
        List<UUID> variantIds = productVariantResponses.stream()
                .map(ProductVariantResponse::getId)
                .collect(Collectors.toList());
        List<ProductVariant> savedVariants = productVariantRepository.findAllById(variantIds);
        savedProductColor.setVariants(savedVariants);

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            savedProductColor.setImages(imageProductService.uploadImages
                    (savedProductColor.getId(), request.getFiles(), request.getAltText()));
        }

        ProductColorResponse response= productColorMapper.toProductColorResponse(savedProductColor);
        response.setVariantResponses(productVariantMapper.toProductVariantResponse(savedProductColor.getVariants()));
        return response;

    }
    @Transactional(rollbackOn = Exception.class)
    public void UploadColorImage(ProductColorRequest request){
        ProductColor productColor = productColorRepository.findById(request.getProductId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ImageProduct> image = imageProductRepository.findAllByProductColor(productColor);

        int totalFiles =
                (image != null ? image.size() : 0)
                        + (request.getFiles() != null ? request.getFiles().size() : 0);
        if (totalFiles > FileUtil.FILE_LIMIT) {
            throw new ApplicationException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
        for(MultipartFile file : request.getFiles()) {
            if (file.getSize()>= FileUtil.MAX_FILE_SIZE_MB){
                throw new ApplicationException(ErrorCode.FILE_SIZE_EXCEEDED);
            }
            ImageProduct imageProduct = ImageProduct.builder()
                    .imageUrl(productColor.getId().toString())
                    .altText(productColor.getProduct().getSlug()+"-"+productColor.getColor().getName())
                    .productColor(productColor)
                    .build();
            UploadImageRequest uploadImageRequest = UploadImageRequest.builder()
                    .imageProduct(imageProduct)
                    .altText(imageProduct.getAltText())
                    .imageUrl(imageProduct.getImageUrl())
                    .file(file)
                    .build();
            imageProductService.uploadImage(uploadImageRequest);
        }
    }
    public List<ImageProductResponse> getImageById(UUID id) {
        List<ImageProduct> imageProduct = imageProductRepository.findAllByProductColorId(id);
        return imageProduct.stream()
                .map(imageProductMapper::toImageProductResponse).collect(Collectors.toList());

    }
    public ProductColorResponse getProductColorById(UUID id){
        ProductColor productColor = productColorRepository.findById(id)
                .orElseThrow(()->new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        return productColorMapper.toProductColorResponse(productColor);
    }

    public List<ProductColorResponse> getAllProductColors() {
        List<ProductColor> colors = productColorRepository.findAll();
        colors.sort(Comparator.comparingInt(pc ->
                pc.getVariants()
                        .stream()
                        .mapToInt(v -> v.getSize().getName()) // giả sử size.name là kiểu int
                        .min()
                        .orElse(Integer.MAX_VALUE)
        ));
        return colors.stream()
                .map(productColorMapper::toProductColorResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductColorResponse updateProductColor(UUID id, UpdateProductColorRequest request) {
        ProductColor existingProductColor = productColorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));

        if (request.getIsAvailable() != null) {
            existingProductColor.setIsAvailable(request.getIsAvailable());
            if(request.getIsAvailable()== Is_Available.NOT_AVAILABLE) {
                List<ProductVariant> variants = productVariantRepository.findAllByproductColor(existingProductColor);
                variants.forEach(variant -> variant.setIsAvailable(request.getIsAvailable()));
                productVariantRepository.saveAll(variants);
            }
        }

        if (request.getColorName() != null) {
            Color color = colorRepository.findByName(request.getColorName())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.COLOR_NOT_FOUND));
            if (!existingProductColor.getColor().equals(color)) {
                if (productColorRepository.existsByProductAndColor(existingProductColor.getProduct(), color)) {
                    throw new ApplicationException(ErrorCode.PRODUCT_COLOR_EXISTED);
                }
                existingProductColor.setColor(color);
            }
        }

        List<ProductVariantRequest> variantsToCreate = new ArrayList<>();
        List<UpdateProductVariantRequest> variantsToUpdate = new ArrayList<>();

        if (request.getVariantRequest() != null) {
            if (!variantsToUpdate.isEmpty()) {
                productVariantService.updateProductVariant(id, variantsToUpdate);
            }

            if (!variantsToCreate.isEmpty()) {

                productVariantService.createListProductVariant(id,request.getVariantRequest());
            }

        }



        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            imageProductService.uploadImages(existingProductColor.getId(), request.getFiles(), request.getAltText());
        }

        ProductColor updatedProductColor = productColorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));

        return productColorMapper.toProductColorResponse(updatedProductColor);
    }

    @Transactional
    public void deleteProductColor(UUID id) {
        ProductColor productColor = productColorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        if(productColor.getImages()!=null){
            for(ImageProduct imgName : productColor.getImages()){
                File file = new File("uploads/" + imgName.getImageUrl());
                if (file.exists()) {
                    file.delete();
                }
            }
        }
//
//        for (ProductVariant variant : productColor.getVariants()) {
//            List<Promotion> promotion = promotionRepository.findAllByProductVariants(variant);
//            promotion.stream().map(p -> {
//                p.getProductVariants().remove(variant);
//                promotionRepository.save(p);
//                return p;
//            }).collect(Collectors.toList());
//            productVariantRepository.delete(variant);
//
//        }

        productColorRepository.delete(productColor);
    }

}