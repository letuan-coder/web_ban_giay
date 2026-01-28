package com.example.DATN.services;

import com.example.DATN.dtos.request.user_address.UpdateUserAddressesRequest;
import com.example.DATN.dtos.request.user_address.UserAddressRequest;
import com.example.DATN.dtos.respone.user_address.UserAddressResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.UserAddressMapper;
import com.example.DATN.models.User;
import com.example.DATN.models.UserAddress;
import com.example.DATN.repositories.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserAddressMapper userAddressMapper;
    private final GetUserByJwtHelper getUserByJwtHelper;

    private String buildFullAddress(UserAddressRequest request) {
        StringJoiner joiner = new StringJoiner(", ");
        add(joiner, request.getStreetDetail());
        add(joiner, request.getWardName());
        add(joiner, request.getDistrictName());
        add(joiner, request.getProvinceName());
        return joiner.toString();
    }

    private void add(StringJoiner joiner, String value) {
        if (value != null && !value.isBlank()) {
            joiner.add(value);
        }
    }

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

        String fullAddress = buildFullAddress(request);
        UserAddress userAddress = UserAddress.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .phoneNumber(request.getPhoneNumber())
                .provinceName(request.getProvinceName())
                .districtName(request.getDistrictName())
                .wardName(request.getWardName())
                .wardCode(request.getWardId())
                .districtCode(request.getDistrictId())
                .provinceCode(request.getProvinceId())
                .userAddress(request.getStreetDetail())
                .streetDetail(request.getStreetDetail())
                .userAddress(fullAddress)
                .Latitude(request.getLat())
                .Longitude(request.getLng())
                .isDefault(request.isDefault())
                .build();
        UserAddress savedAddress = userAddressRepository.save(userAddress);
        return userAddressMapper.toResponse(savedAddress);
    }

    public List<UserAddressResponse> getUserAddresses() {
        User user = getUserByJwtHelper.getCurrentUser();
        List<UserAddress> userAddress = userAddressRepository.findByUser(user);
        return userAddressMapper.toResponseList(userAddress);
    }

    @Transactional
    public UserAddressResponse updateUserAddress(UUID addressId, UpdateUserAddressesRequest request) {
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
