package com.example.DATN.services;

import com.example.DATN.dtos.request.StoreRequest;
import com.example.DATN.dtos.respone.StoreResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.StoreMapper;
import com.example.DATN.models.Store;
import com.example.DATN.repositories.StoreRepository;
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
    private final String storeCode="CN";
    public StoreResponse createStore(StoreRequest request) {
        Long totalStore=storeRepository.count();
        String code = storeCode + (totalStore + 1);
        Store store = storeMapper.toStore(request);
        store.setCode(code);
        store = storeRepository.save(store);
        return storeMapper.toStoreResponse(store);
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

    public void deleteStore(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        storeRepository.delete(store);
    }
}
