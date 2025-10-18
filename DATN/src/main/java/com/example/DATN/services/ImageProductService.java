
package com.example.DATN.services;

import com.example.DATN.constant.Util.FileUtil;
import com.example.DATN.dtos.respone.ImageProductResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ImageProductMapper;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.models.ImageProduct;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.repositories.ImageProductRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageProductService {
    //REPOSITORIES
    private final FileStorageService fileStorageService;

    private final ImageProductRepository imageProductRepository;

    private final ProductVariantRepository productVariantRepository;
    private final ImageProductMapper imageProductMapper;
    private final ProductVariantMapper productVariantMapper;

    public static boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("png")
                || extension.equals("webp")
                || extension.equals("gif");
    }

    public List<ImageProductResponse> uploadImages
            (UUID productVariantId,
             List<MultipartFile> files,
             List<String> altTexts) {
        Integer count = productVariantRepository.countById(productVariantId);
        if (count > FileUtil.FILE_LIMIT) {
            throw new ApplicationException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        String productSlug = productVariant.getProduct().getSlug();
        List<ImageProduct> newImages = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (!isImageFile(file)) {
                throw new ApplicationException(ErrorCode.INVALID_FILE_TYPE);
            }
            String altText = (altTexts != null && i < altTexts.size())
                    ? altTexts.get(i) : productVariant.getProduct().getName(); // Default altText to product name

            // 2. Store the file using FileStorageService with product slug
            String generatedFileName = fileStorageService.storeFile(file, productSlug);
            String imageUrl = UriComponentsBuilder.fromPath("/uploads/")
                    .path(generatedFileName)
                    .build()
                    .toUriString();
            ImageProduct imageProduct = ImageProduct.builder()
                    .productVariant(productVariant)
                    .altText(altText)
                    .imageUrl(imageUrl)
                    .build();
            newImages.add(imageProduct);
        }
        List<ImageProduct> savedImages = imageProductRepository.saveAll(newImages);
        return savedImages.stream()
                .map(imageProductMapper::toImageProductResponse)
                .toList();
    }
//
//    public void deleteImage(Long imageId) {
//        ImageProduct imageProduct = imageProductRepository.findById(imageId)
//                .orElseThrow(() -> new ApplicationException(ErrorCode.IMAGE_NOT_FOUND));
//
//        // Delete physical file
//        fileStorageService.deleteFile(imageProduct.getImageUrl());
//
//        // Delete database record
//        imageProductRepository.delete(imageProduct);
//    }
}
