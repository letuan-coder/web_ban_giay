package com.example.DATN.services;

import com.example.DATN.constant.BannerType;
import com.example.DATN.dtos.request.BannerRequest;
import com.example.DATN.dtos.request.UploadImageRequest;
import com.example.DATN.dtos.request.banner.BannerSortRequest;
import com.example.DATN.dtos.respone.BannerResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.BannerMapper;
import com.example.DATN.models.Banner;
import com.example.DATN.repositories.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;
    private final ImageProductService imageProductService;

    public BannerResponse createBanner(BannerRequest request) {
        LocalDate endAt = request.getEndAt();
        if (endAt == null) {
            endAt = LocalDate.of(9999, 12, 31);
        }
        if(request.getStartAt()==null){
            request.setStartAt(LocalDate.of(1, 1, 1));        }
        Banner banner = Banner.builder()
                .bannerName(request.getBannerName())
                .sortOrder(request.getSortOrder())
                .startAt(request.getStartAt())
                .redirectUrl(request.getRedirectUrl())
                .endAt(endAt)
                .active(request.getActive())
                .type(request.getType())
                .imageUrl("")
                .build();
        Banner savedBanner = bannerRepository.save(banner);
        UploadImageRequest uploadImageRequest = UploadImageRequest.builder()
                .file(request.getFile())
                .banner(savedBanner)
                .imageProduct(null)
                .product(null)
                .altText(banner.getBannerName())
                .build();
        imageProductService.uploadImage(uploadImageRequest);
        return bannerMapper.toBannerResponse(savedBanner);
    }

    public List<BannerResponse> getBannersByType(BannerType type) {
        return bannerRepository.findByType(type).stream()
                .map(bannerMapper::toBannerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void sortOrderBanner(List<BannerSortRequest> requests) {
        for (BannerSortRequest req : requests) {
            Banner banner = bannerRepository.findById(req.getId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.BANNER_NOT_FOUND));
            banner.setType(req.getType());
            banner.setSortOrder(req.getSortOrder());
            bannerRepository.save(banner);
        }
    }

    public List<BannerResponse> getAllBanners() {
        List<Banner> banners =  bannerRepository.findAll();
        banners.forEach(banner -> {
            if (banner.getEndAt() != null
                    && banner.getEndAt().isBefore(LocalDate.now().plusDays(1))) {
                banner.setActive(false);
                banner.setType(BannerType.INACTIVE);
                bannerRepository.save(banner);
            }
        });
        return banners.stream()
                .map(bannerMapper::toBannerResponse)
                .collect(Collectors.toList());
    }

    public BannerResponse updateBanner(UUID id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BANNER_NOT_FOUND));
       request.setImageUrl(banner.getImageUrl());
        if(request.getEndAt()==null){
             request.setEndAt(LocalDate.of(9999, 12, 31));
        }
        if(request.getStartAt()==null){
            request.setStartAt(LocalDate.of(1, 1, 1));        }
        if(request.getFile()!=null){
            UploadImageRequest uploadImageRequest =UploadImageRequest.builder()
                    .file(request.getFile())
                    .banner(banner)
                    .imageProduct(null)
                    .product(null)
                    .altText(banner.getBannerName())
                    .build();
            imageProductService.uploadImage(uploadImageRequest);
            request.setImageUrl(banner.getImageUrl());
        }
        bannerMapper.updateBanner(banner, request);
        return bannerMapper.toBannerResponse(bannerRepository.save(banner));
    }

    public void deleteBanner(UUID id) {
        imageProductService.deleteImage(id);
        if (!bannerRepository.existsById(id)) {
            throw new ApplicationException(ErrorCode.BANNER_NOT_FOUND);
        }
        bannerRepository.deleteById(id);
    }
}
