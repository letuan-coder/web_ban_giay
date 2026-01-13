package com.example.DATN.services;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.constant.StockType;
import com.example.DATN.constant.TransactionStatus;
import com.example.DATN.constant.TransactionType;
import com.example.DATN.dtos.request.StockRequest;
import com.example.DATN.dtos.request.StockTransactionItemReceivedRequest;
import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.dtos.respone.StockTransactionResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.StockMapper;
import com.example.DATN.mapper.StockTransactionMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.StockRepository;
import com.example.DATN.repositories.StockTransactionItemRepository;
import com.example.DATN.repositories.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final StockTransactionRepository stockTransactionRepository;
    private final StockTransactionItemRepository stockTransactionItemRepository;
    private final ProductVariantRepository productVariantRepository;

   private final StockTransactionMapper stockTransactionMapper;

    @Transactional
    public void createStockForStore(Store store,ProductVariant variant, Integer quantity) {
            boolean exists = stockRepository
                    .existsByStoreAndVariantAndStockType(store, variant, StockType.STORE);
            if (exists){
                Stock stockOfVariant = stockRepository.findByVariant_IdAndStore(variant.getId(),store)
                        .orElseThrow(()->new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
                stockOfVariant.setQuantity(stockOfVariant.getQuantity()+quantity);
            }
            else {
                Stock stock = Stock.builder()
                        .store(store)
                        .variant(variant)
                        .stockType(StockType.STORE)
                        .quantity(quantity)
                        .sellableQuantity(quantity-10)
                        .build();
                stockRepository.save(stock);
            }
    }
    @Transactional
    public void createStockForWarehouse(WareHouse warehouse, ProductVariant variant,Integer quantity) {
        boolean exists = stockRepository.existsByWarehouseAndVariantAndStockType(warehouse,variant,StockType.WAREHOUSE);
        if (exists){
            Stock stockOfVariant = stockRepository.findByVariant_IdAndWarehouse(variant.getId(),warehouse)
                    .orElseThrow(()->new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
            stockOfVariant.setQuantity(stockOfVariant.getQuantity()+quantity);
        }
        else {
            Stock stock = Stock.builder()
                    .warehouse(warehouse)
                    .variant(variant)
                    .stockType(StockType.WAREHOUSE)
                    .quantity(quantity)
                    .sellableQuantity(quantity-10)
                    .build();
            stockRepository.save(stock);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public StockTransactionResponse createStock(StockRequest request) {
        StockTransaction stockTransaction = stockTransactionRepository
                .findByCode(request.getTransactionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
        if (stockTransaction.getStatus() != TransactionStatus.PENDING) {
            throw new ApplicationException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        if (stockTransaction.getType() == TransactionType.IMPORT_TO_WAREHOUSE) {
            for (StockTransactionItemReceivedRequest receivedRequest
                    : request.getStockTransactionItemId()) {
                ProductVariant variant = productVariantRepository
                        .findById(receivedRequest.getStockTransactionId())
                        .orElseThrow(() ->
                                new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));

                StockTransactionItem item = stockTransactionItemRepository
                        .findByVariantAndTransaction(variant, stockTransaction);
               Optional<Stock> stockOtp = stockRepository.findByVariant_IdAndWarehouse
                       (variant.getId(),item.getTransaction().getToWareHouse());
                if(stockOtp.isPresent()) {
                    Stock stock = stockOtp.get();
                    stock.setQuantity(stock.getQuantity() +receivedRequest.getReceivedQuantity());
                    item.setOriginalTransactionItem(item);

                }
                else {
                    createStockForWarehouse(stockTransaction.getToWareHouse(),
                            variant,receivedRequest.getReceivedQuantity());
                }
            }
        } else {
            List<StockTransactionItem> items = stockTransaction.getItems();
            for (StockTransactionItem item : items) {
                Stock stock = stockRepository.findByVariant_IdAndStore(item.getVariant().getId(),stockTransaction.getToStore())
                        .orElseThrow(()->new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
                stock.setQuantity(stock.getQuantity()+item.getQuantity());
                item.getVariant().setIsAvailable(Is_Available.AVAILABLE);
                createStockForStore(stockTransaction.getToStore(),item.getVariant(),item.getQuantity());
                productVariantRepository.save(item.getVariant());
            }
        }
        stockTransaction.setStatus(TransactionStatus.COMPLETED);
        StockTransaction saved=  stockTransactionRepository.save(stockTransaction);
        StockTransactionResponse  response =stockTransactionMapper.toStockTransactionResponse(saved);

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