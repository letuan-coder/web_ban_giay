package com.example.DATN.services;

import com.example.DATN.constant.Util.FileUtil;
import com.example.DATN.dtos.request.StoreFileRequest;
import com.example.DATN.dtos.request.UploadImageRequest;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.Format_ImageUrl_Helper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageProductService {
    private final FileStorageService fileStorageService;
    private final ImageProductRepository imageProductRepository;
    private final ProductRepository productRepository;
    private final String defaultImage = "default.png";
    private final ProductColorRepository productColorRepository;
    private final BannerRepository bannerRepository;
    private final Format_ImageUrl_Helper formatImageUrlHelper;
    private final UserRepository userRepository;

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
                .findById(ProductColorId).orElseThrow(() ->
                        new ApplicationException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
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
            ImageProduct imageProduct = ImageProduct.builder()
                    .productColor(productColor)
                    .altText(altText)
                    .imageUrl("")
                    .build();
            ImageProduct savedImage = imageProductRepository.save(imageProduct);
            StoreFileRequest storeFileRequest = StoreFileRequest.builder()
                    .file(file)
                    .imageProduct(imageProduct)
                    .fileName(productSlug)
                    .build();
            String generatedFileName = fileStorageService.storeFile(storeFileRequest);
            savedImage.setImageUrl(generatedFileName);
            newImages.add(imageProduct);
        }
        return newImages;
    }

    public void uploadImage(UploadImageRequest request) {
        if (!isImageFile(request.getFile())) {
            throw new ApplicationException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        MultipartFile file = request.getFile();
        String fileName = null;
        if (request.getImageProduct() != null) {
            ImageProduct imageProduct = request.getImageProduct();
            StoreFileRequest storeRequest = StoreFileRequest.builder()
                    .imageProduct(imageProduct)
                    .file(request.getFile())
                    .fileName(imageProduct.getAltText())
                    .build();
            fileName = fileStorageService.storeFile(storeRequest);
            imageProduct.setImageUrl(fileName);
            imageProduct.setAltText(request.getAltText());
            imageProductRepository.save(imageProduct);
        } else if (request.getProduct() != null) {
            Product product = request.getProduct();
            StoreFileRequest storeRequest = StoreFileRequest.builder()
                    .file(request.getFile())
                    .fileName(product.getSlug())
                    .product(product)
                    .build();
            product.setThumbnailUrl(fileStorageService.storeFile(storeRequest));
            productRepository.save(product);

        } else if (request.getBanner() != null) {
            Banner banner = request.getBanner();
            fileName = formatImageUrlHelper.toSlug(banner.getBannerName());
            StoreFileRequest storeRequest = StoreFileRequest.builder()
                    .banner(banner)
                    .file(request.getFile())
                    .fileName(fileName)
                    .build();
            banner.setImageUrl(fileStorageService.storeFile(storeRequest));
            bannerRepository.save(banner);
        } else if (request.getUserAvatar() != null) {
            User user = request.getUserAvatar();
            StoreFileRequest storeRequest = StoreFileRequest.builder()
                    .userAvatar(user)
                    .file(request.getFile())
                    .fileName("avatar_" + user.getId())
                    .build();
            user.setUserImage(fileStorageService.storeFile(storeRequest));
            userRepository.save(user);
        } else {
            throw new ApplicationException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public void deleteImage(UUID id) {
        Optional<ImageProduct> imageProductOpt = imageProductRepository.
                findById(id);
        Optional<Product> thumbnailProduct = productRepository.findById(id);
        if (imageProductOpt.isPresent())//checking productColor'image is exsited
        {
            ImageProduct imageProduct = imageProductOpt.get();
            fileStorageService.deleteFile(imageProduct.getImageUrl());
            imageProduct.setImageUrl(defaultImage);
            imageProductRepository.delete(imageProduct);
        } else if (thumbnailProduct.isPresent())//checking thumbnail of product is exsited
        {
            if (thumbnailProduct.isPresent()) {
                Product product = thumbnailProduct.get();
                fileStorageService.deleteFile(product.getThumbnailUrl());
                product.setThumbnailUrl(defaultImage);
                productRepository.save(product);
            }
        } else if (imageProductOpt.isEmpty() && thumbnailProduct.isEmpty())//checking banner images is existed
        {
            Optional<Banner> bannerOpt = bannerRepository.findById(id);
            if (bannerOpt.isPresent()) {
                Banner banner = bannerOpt.get();
                fileStorageService.deleteFile(banner.getImageUrl());
                banner.setImageUrl(defaultImage);
                bannerRepository.save(banner);
            }
        } else {
            throw new ApplicationException(ErrorCode.IMAGE_NOT_FOUND);
        }


    }
}