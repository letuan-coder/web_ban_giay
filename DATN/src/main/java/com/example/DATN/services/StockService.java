package com.example.DATN.services;

import com.example.DATN.constant.StockType;
import com.example.DATN.constant.TransactionStatus;
import com.example.DATN.constant.TransactionType;
import com.example.DATN.dtos.request.StockRequest;
import com.example.DATN.dtos.request.StockTransactionItemReceivedRequest;
import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.StockMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final StockTransactionRepository stockTransactionRepository;
    private final StockTransactionItemRepository stockTransactionItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final UserAddressRepository userAddressRepository;
    private final GetUserByJwtHelper getUserByJwtHelper;
    private final WareHouseRepository wareHouseRepository;

    @Transactional
    public void createStockForStore(UUID storeId, Integer minQuantity) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        List<Stock> stocks = new ArrayList<>();
        List<ProductVariant> productVariantList = productVariantRepository.findAll();
        for (ProductVariant variant : productVariantList) {
            boolean exists = stockRepository
                    .existsByStoreAndVariantAndStockType(store, variant, StockType.STORE);
            if (exists) continue;
            Stock stock = Stock.builder()
                    .store(store)
                    .variant(variant)
                    .stockType(StockType.STORE)
                    .quantity(0)
                    .minQuantity(minQuantity)
                    .build();
            stocks.add(stock);
        }
        stockRepository.saveAll(stocks);

    }
    @Transactional
    public void createStockForWarehouse(UUID warehouseId, Integer minQuantity) {
        WareHouse wareHouse = wareHouseRepository.findById(warehouseId)
                .orElseThrow(()->new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        List<Stock> stocks = new ArrayList<>();
        List<ProductVariant> productVariantList = productVariantRepository.findAll();
        Long value = productVariantList.stream().count();
        Long realmin =minQuantity.longValue()/value;
        for (ProductVariant variant : productVariantList) {
            boolean exists = stockRepository
                    .existsByWarehouseAndVariantAndStockType(wareHouse, variant, StockType.STORE);
            if (exists) continue;
            Stock stock = Stock.builder()
                    .warehouse(wareHouse)
                    .variant(variant)
                    .stockType(StockType.STORE)
                    .quantity(0)
                    .minQuantity(realmin.compareTo(realmin))
                    .build();
            stocks.add(stock);
        }
        stockRepository.saveAll(stocks);

    }

    @Transactional(rollbackFor = Exception.class)
    public StockResponse createStock(StockRequest request) {
        StockTransaction stockTransaction = stockTransactionRepository
                .findByCode(request.getTransactionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
        StockResponse response = null;
        if (stockTransaction.getStatus() != TransactionStatus.PENDING) {
            throw new ApplicationException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        if (stockTransaction.getType() == TransactionType.IMPORT_TO_WAREHOUSE) {
            for (StockTransactionItemReceivedRequest receivedRequest
                    : request.getStockTransactionItemId()) {
                ProductVariant variant = productVariantRepository
                        .findById(receivedRequest.getStockTransactionId())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
                StockTransactionItem item = stockTransactionItemRepository
                        .findByVariantAndTransaction(variant, stockTransaction);
               Optional<Stock> stockOtp = stockRepository.findByVariantAndWarehouse
                       (variant,item.getTransaction().getToWareHouse());
                if(stockOtp.isPresent()) {
                    Stock stock = stockOtp.get();
                    stock.setQuantity(stock.getQuantity() + item.getQuantity());
                    stockTransaction.setStatus(TransactionStatus.COMPLETED);
                    Stock savedStockForWareHouse = stockRepository.save(stock);
                    item.setOriginalTransactionItem(item);
                    response = stockMapper.toStockResponse(savedStockForWareHouse);
                }

            }
        } else {
            List<StockTransactionItem> items = stockTransaction.getItems();
            for (StockTransactionItem item : items) {
                Stock stock = stockRepository.findByVariantAndStore(item.getVariant(),stockTransaction.getToStore())
                        .orElseThrow(()->new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
                stock.setQuantity(stock.getQuantity()+item.getQuantity());

                Stock savedStockForStore = stockRepository.save(stock);
                response = stockMapper.toStockResponse(savedStockForStore);
            }
            stockTransaction.setStatus(TransactionStatus.COMPLETED);

        }
        return response;
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


    //    public StockResponse updateStock(Long id, StockRequest request) {
//        Stock stock = stockRepository.findById(id)
//                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
//
//        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
//                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
//
//        stock.setVariant(variant);
//        stock.setStockType(request.getStockType());
//        stock.setQuantity(request.getQuantity());
//
//        if (request.getStoreId() != null) {
//            Store store = storeRepository.findById(request.getStoreId())
//                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
//            stock.setStore(store);
//        } else {
//            stock.setStore(null);
//        }
//
//        if (request.getWarehouseId() != null) {
//            WareHouse warehouse = wareHouseRepository.findById(request.getWarehouseId())
//                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
//            stock.setWarehouse(warehouse);
//        } else {
//            stock.setWarehouse(null);
//        }
//
//        stock = stockRepository.save(stock);
//        return stockMapper.toStockResponse(stock);
//    }
//
    public void deleteStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
        stockRepository.delete(stock);
    }
}