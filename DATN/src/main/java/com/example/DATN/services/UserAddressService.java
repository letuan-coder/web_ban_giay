package com.example.DATN.services;

import com.example.DATN.dtos.request.user_address.UserAddressRequest;
import com.example.DATN.dtos.respone.user_address.UserAddressResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.UserAddressMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.CommuneRepository;
import com.example.DATN.repositories.DistrictRepository;
import com.example.DATN.repositories.ProvinceRepository;
import com.example.DATN.repositories.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final UserAddressMapper userAddressMapper;
    private final GetUserByJwtHelper getUserByJwtHelper;

    @Transactional(rollbackFor = Exception.class)
    public UserAddressResponse createUserAddress(UserAddressRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        if (request.isDefault()) {
            userAddressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(oldDefault -> {
                    oldDefault.setDefault(false);
                    userAddressRepository.save(oldDefault);
                });
        }
        Province province = provinceRepository.findById(request.getProvinceId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.PROVINCE_NOT_FOUND));
        District district = districtRepository.findById(request.getDistrictId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.DISTRICT_NOT_FOUND));
        Commune commune = communeRepository.findById(request.getCommuneId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.COMMUNE_NOT_FOUND));
        if(!commune.getParentCode().equals(request.getDistrictId()) &&
                !district.getParentCode().equals(request.getProvinceId())){
            throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        String fullAddress = String.format("%s,%s, %s, %s",
                request.getStreetDetail(),
                commune.getNameWithType(),
                district.getNameWithType(),
                province.getNameWithType());
        UserAddress userAddress= UserAddress.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .phoneNumber(request.getPhoneNumber())
                .province(province)
                .district(district)
                .commune(commune)
                .userAddress(fullAddress)
                .streetDetail(request.getStreetDetail())
                .isDefault(request.isDefault())
                .build();
        UserAddress savedAddress = userAddressRepository.save(userAddress);
        UserAddressResponse response= userAddressMapper.toResponse(savedAddress);
        response.setUserAddress(fullAddress);
        return response;
    }

    public List<UserAddressResponse> getUserAddresses() {
        User user = getUserByJwtHelper.getCurrentUser();
        List<UserAddress> addresses = userAddressRepository.findByUser(user);
        return addresses.stream()
            .map(userAddressMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserAddressResponse updateUserAddress(UUID addressId, UserAddressRequest request) {
        UserAddress existingAddress = userAddressRepository.findById(addressId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND));

        User user = getUserByJwtHelper.getCurrentUser();
        if (!existingAddress.getUser().getId().equals(user.getId())) {
            throw new ApplicationException(ErrorCode.ACCESS_DENIED);
        }

        if (request.isDefault() && !existingAddress.isDefault()) {
            userAddressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(oldDefault -> {
                    oldDefault.setDefault(false);
                    userAddressRepository.save(oldDefault);
                });
        }

        userAddressMapper.updateUserAddress(existingAddress, request);

        Province province = provinceRepository.findById(request.getProvinceId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.PROVINCE_NOT_FOUND));
        District district = districtRepository.findById(request.getDistrictId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.DISTRICT_NOT_FOUND));
        Commune commune = communeRepository.findById(request.getCommuneId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.COMMUNE_NOT_FOUND));

        existingAddress.setProvince(province);
        existingAddress.setDistrict(district);
        existingAddress.setCommune(commune);

        UserAddress updatedAddress = userAddressRepository.save(existingAddress);
        return userAddressMapper.toResponse(updatedAddress);
    }

    public void deleteUserAddress(UUID addressId) {
        UserAddress address = userAddressRepository.findById(addressId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND));

        User user = getUserByJwtHelper.getCurrentUser();
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ApplicationException(ErrorCode.ACCESS_DENIED);
        }

        userAddressRepository.delete(address);
    }

    @Transactional
    public void setDefaultAddress(UUID addressId) {
        User user = getUserByJwtHelper.getCurrentUser();
        UserAddress newDefaultAddress = userAddressRepository.findById(addressId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!newDefaultAddress.getUser().getId().equals(user.getId())) {
            throw new ApplicationException(ErrorCode.ACCESS_DENIED);
        }

        userAddressRepository.findByUserAndIsDefaultTrue(user)
            .ifPresent(oldDefault -> {
                if (!oldDefault.getId().equals(addressId)) {
                    oldDefault.setDefault(false);
                    userAddressRepository.save(oldDefault);
                }
            });

        if (!newDefaultAddress.isDefault()) {
            newDefaultAddress.setDefault(true);
            userAddressRepository.save(newDefaultAddress);
        }
    }
}
