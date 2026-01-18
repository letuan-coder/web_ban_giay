package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.dtos.request.StoreRequest;
import com.example.DATN.dtos.respone.StoreResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.StoreMapper;
import com.example.DATN.models.Store;
import com.example.DATN.repositories.StoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final GhnService ghnService;
    private final String storeCode = "CN";
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Transactional
    public StoreResponse createStore(StoreRequest request) throws JsonProcessingException {
        Long code = snowflakeIdGenerator.nextId();
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }
        String storeId = storeCode + code;
        Store store = storeMapper.toStore(request);

        store.setCode(storeId);
        ghnService.registerShop(store);
        Store storesaved = storeRepository.save(store);
        return storeMapper.toStoreResponse(storesaved);
    }

    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll()
                .stream()
                .map(storeMapper::toStoreResponse)
                .collect(Collectors.toList());
    }

    public Set<Store> getAllStore() {
        return new HashSet<>(storeRepository.findAll());
    }

    public StoreResponse getStoreById(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        return storeMapper.toStoreResponse(store);
    }

    public StoreResponse updateStore(UUID id, StoreRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        storeMapper.updateStore(store, request);
        store = storeRepository.save(store);
        return storeMapper.toStoreResponse(store);
    }

    public void deleteStore(String storeid) {
        UUID id = UUID.fromString(storeid);
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        storeRepository.delete(store);
    }
}
