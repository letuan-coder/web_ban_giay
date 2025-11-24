package com.example.DATN.services;

import com.example.DATN.dtos.request.StockRequest;
import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.StockMapper;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Stock;
import com.example.DATN.models.Store;
import com.example.DATN.models.WareHouse;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.StockRepository;
import com.example.DATN.repositories.StoreRepository;
import com.example.DATN.repositories.WareHouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final WareHouseRepository wareHouseRepository;
    private final StockMapper stockMapper;

    public StockResponse createStock(StockRequest request) {
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        Stock stock = new Stock();
        stock.setVariant(variant);
        stock.setStockType(request.getStockType());
        stock.setQuantity(request.getQuantity());

        if (request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            stock.setStore(store);
        }

        if (request.getWarehouseId() != null) {
            WareHouse warehouse = wareHouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
            stock.setWarehouse(warehouse);
        }

        stock = stockRepository.save(stock);
        return stockMapper.toStockResponse(stock);
    }

    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll()
                .stream()
                .map(stockMapper::toStockResponse)
                .collect(Collectors.toList());
    }

    public StockResponse getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
        return stockMapper.toStockResponse(stock);
    }

    public StockResponse updateStock(Long id, StockRequest request) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        stock.setVariant(variant);
        stock.setStockType(request.getStockType());
        stock.setQuantity(request.getQuantity());

        if (request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            stock.setStore(store);
        } else {
            stock.setStore(null);
        }

        if (request.getWarehouseId() != null) {
            WareHouse warehouse = wareHouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
            stock.setWarehouse(warehouse);
        } else {
            stock.setWarehouse(null);
        }

        stock = stockRepository.save(stock);
        return stockMapper.toStockResponse(stock);
    }

    public void deleteStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
        stockRepository.delete(stock);
    }
}