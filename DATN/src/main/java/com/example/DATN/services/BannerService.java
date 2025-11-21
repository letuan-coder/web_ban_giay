package com.example.DATN.services;

import com.example.DATN.constant.BannerType;
import com.example.DATN.dtos.request.BannerRequest;
import com.example.DATN.dtos.respone.BannerResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.BannerMapper;
import com.example.DATN.models.Banner;
import com.example.DATN.repositories.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        Banner banner = Banner.builder()
                .bannerName(request.getBannerName())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .active(true)
                .build();
        Banner savedBanner=bannerRepository.save(banner);
        imageProductService.uploadBannerImages(savedBanner.getId(),request.getFile());
        return bannerMapper.toBannerResponse(savedBanner);
    }

    public List<BannerResponse> getBannersByType(BannerType type) {
        return bannerRepository.findByType(type).stream()
                .map(bannerMapper::toBannerResponse)
                .collect(Collectors.toList());
    }

    public List<BannerResponse> getAllBanners() {
               return bannerRepository.findAll().stream()
                .map(bannerMapper::toBannerResponse)
                .collect(Collectors.toList());
    }

    public BannerResponse updateBanner(UUID id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BANNER_NOT_FOUND));
        bannerMapper.updateBanner(banner, request);
        return bannerMapper.toBannerResponse(bannerRepository.save(banner));
    }

    public void deleteBanner(UUID id) {
        if (!bannerRepository.existsById(id)) {
            throw new ApplicationException(ErrorCode.BANNER_NOT_FOUND);
        }
        bannerRepository.deleteById(id);
    }
}
