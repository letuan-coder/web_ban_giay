package com.example.DATN.services;

import com.example.DATN.constant.Util.FileUtil;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ImageProductMapper;
import com.example.DATN.models.Banner;
import com.example.DATN.models.ImageProduct;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductColor;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageProductService {
    private final FileStorageService fileStorageService;
    private final ImageProductRepository imageProductRepository;
    private final ColorRepository colorRepository;
    private final ProductRepository productRepository;
    private final ImageProductMapper imageProductMapper;
    private final ProductColorRepository productColorRepository;
    private final BannerRepository bannerRepository;

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

    public List<ImageProduct> uploadImages(
            UUID ProductColorId,
            List<MultipartFile> files,
            List<String> altTexts) {
        ProductColor productColor = productColorRepository
                .findById(ProductColorId).orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
        if (files.size() > FileUtil.FILE_LIMIT) {
            throw new ApplicationException(ErrorCode.FILE_COUNT_EXCEEDED);
        }

        String productSlug = productColor.getProduct().getSlug();
        List<ImageProduct> newImages = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (!isImageFile(file)) {
                throw new ApplicationException(ErrorCode.INVALID_FILE_TYPE);
            }
            String altText = (altTexts != null && i < altTexts.size())
                    ? altTexts.get(i) : productColor.getProduct().getName(); // Default altText to product name

            String generatedFileName = fileStorageService.storeFile(file, productSlug);
            ImageProduct imageProduct = ImageProduct.builder()
                    .productColor(productColor)
                    .altText(altText)
                    .imageUrl(generatedFileName)
                    .build();
            newImages.add(imageProduct);
        }
        return imageProductRepository.saveAll(newImages);
    }

    public void uploadThumbnailImages(
            UUID ProductId,
            MultipartFile file)
    {
        Product product = productRepository.findById(ProductId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        String productSlug = product.getSlug();

        String altText = productSlug;
        product.setAltText(altText);

        String generatedFileName = fileStorageService.storeThumbnailFile(file, productSlug);
        product.setThumbnailUrl(generatedFileName);

    }

    public void uploadBannerImages(
            UUID BannerId,
            MultipartFile file) {
        Banner banner = bannerRepository.findById(BannerId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BANNER_NOT_FOUND));
        String BannerFileName = banner.getBannerName();
        String bannerUrl = fileStorageService.storeBannerFile(file, BannerFileName);
        banner.setImageUrl(bannerUrl);
    }


    public void deleteImage(Long imageId) {
        ImageProduct imageProduct = imageProductRepository.findById(imageId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.IMAGE_NOT_FOUND));
        fileStorageService.deleteFile(imageProduct.getImageUrl());
        imageProductRepository.delete(imageProduct);
    }
}