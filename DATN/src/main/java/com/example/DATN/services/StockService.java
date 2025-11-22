package com.example.DATN.services;

import com.example.DATN.constant.StockType;
import com.example.DATN.dtos.request.AddStockRequest;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.Stock;
import com.example.DATN.models.Store;
import com.example.DATN.models.WareHouse;
import com.example.DATN.repositories.StoreRepository;
import com.example.DATN.repositories.WareHouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final WareHouseRepository wareHouseRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public Stock createStockForWareHouse(AddStockRequest request){
        if(request.getStockType()== StockType.WAREHOUSE){
            WareHouse wareHouse = wareHouseRepository.findById(request.getLocationId())
                    .orElseThrow(()->new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
            Stock stock = Stock.builder()
                    .quantity(request.getQuantity())
                    .warehouse(wareHouse)
                    .store(null)
                    .build();
            return stock;
        }
        else{
            Store store = storeRepository.findById(request.getLocationId())
                    .orElseThrow(()->new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            Stock stock = Stock.builder()
                    .quantity(request.getQuantity())
                    .warehouse(null)
                    .store(store)
                    .build();
            return stock;
        }
    }
}
