package com.example.DATN.services;

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

    public WareHouseResponse createWareHouse(WareHouseRequest request) {
        WareHouse wareHouse = WareHouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
        
        wareHouse = wareHouseRepository.save(wareHouse);
        return wareHouseMapper.toWareHouseResponse(wareHouse);
    }

    public List<WareHouseResponse> getAllWareHouses() {
        return wareHouseRepository.findAll().stream()
                .map(wareHouseMapper::toWareHouseResponse)
                .collect(Collectors.toList());
    }

    public WareHouseResponse getWareHouseById(Long id) {
        WareHouse wareHouse = wareHouseRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return wareHouseMapper.toWareHouseResponse(wareHouse);
    }

    public WareHouseResponse updateWareHouse(Long id, WareHouseRequest request) {
        WareHouse wareHouse = wareHouseRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        
        wareHouse.setName(request.getName());
        wareHouse.setLocation(request.getLocation());
        wareHouse.setCapacity(request.getCapacity());
        wareHouse.setUpdatedAt(LocalDateTime.now());

        wareHouse = wareHouseRepository.save(wareHouse);
        return wareHouseMapper.toWareHouseResponse(wareHouse);
    }

    public void deleteWareHouse(Long id) {
        WareHouse wareHouse = wareHouseRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        wareHouse.setDeleted(true);
        wareHouseRepository.save(wareHouse);
    }
}
