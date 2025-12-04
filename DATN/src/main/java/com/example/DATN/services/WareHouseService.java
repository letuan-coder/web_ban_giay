package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.dtos.request.WareHouseRequest;
import com.example.DATN.dtos.respone.WareHouseResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.WareHouseMapper;
import com.example.DATN.models.WareHouse;
import com.example.DATN.repositories.WareHouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WareHouseService {

    private final WareHouseRepository wareHouseRepository;
    private final WareHouseMapper wareHouseMapper;
    private final String wareHousePrefix = "WH";
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    public WareHouseResponse createWareHouse(WareHouseRequest request) {
        Long snowflake = snowflakeIdGenerator.nextId();
        String code =wareHousePrefix+(snowflake);
        WareHouse wareHouse = WareHouse.builder()
                .warehouseCode(code)
                .addressDetail(request.getAddressDetail())
                .provinceCode(request.getProvinceCode())
                .districtCode(request.getDistrictCode())
                .wardCode(request.getWardCode())
                .name(request.getName())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .stocks(null)
                .deleted(false)
                .build();
        wareHouse = wareHouseRepository.save(wareHouse);
        return wareHouseMapper.toWareHouseResponse(wareHouse);
    }

    public List<WareHouseResponse> getAllWareHouses() {
        return wareHouseRepository.findAllByDeletedFalse().stream()
                .map(wareHouseMapper::toWareHouseResponse)
                .collect(Collectors.toList());
    }

    public WareHouseResponse getWareHouseById(String warehouseCode) {
        WareHouse wareHouse = wareHouseRepository.findBywarehouseCode(warehouseCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return wareHouseMapper.toWareHouseResponse(wareHouse);
    }

    public WareHouseResponse updateWareHouse(String id, WareHouseRequest request) {
        WareHouse wareHouse = wareHouseRepository.findBywarehouseCode(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        wareHouse.setDistrictCode(request.getDistrictCode());
        wareHouse.setProvinceCode(request.getProvinceCode());
        wareHouse.setWardCode(request.getWardCode());
        wareHouse.setName(request.getName());
        wareHouse.setLocation(request.getLocation());
        wareHouse.setCapacity(request.getCapacity());
        wareHouse.setUpdatedAt(LocalDateTime.now());

        wareHouse = wareHouseRepository.save(wareHouse);
        return wareHouseMapper.toWareHouseResponse(wareHouse);
    }

    public void deleteWareHouse(String code) {
        WareHouse wareHouse = wareHouseRepository.findBywarehouseCode(code)
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        wareHouse.setDeleted(true);
        wareHouseRepository.save(wareHouse);
    }
}
