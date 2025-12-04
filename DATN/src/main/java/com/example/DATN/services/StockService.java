package com.example.DATN.services;

import com.example.DATN.constant.StockType;
import com.example.DATN.constant.TransactionType;
import com.example.DATN.dtos.request.StockRequest;
import com.example.DATN.dtos.request.StockTransactionItemReceivedRequest;
import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.StockMapper;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Stock;
import com.example.DATN.models.StockTransaction;
import com.example.DATN.models.StockTransactionItem;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.StockRepository;
import com.example.DATN.repositories.StockTransactionItemRepository;
import com.example.DATN.repositories.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final StockTransactionRepository stockTransactionRepository;
    private final StockTransactionItemRepository stockTransactionItemRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional(rollbackFor = Exception.class)
    public StockResponse createStock(StockRequest request) {
//        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
//                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
//
//        StockTransaction stockTransaction = stockTransactionRepository
//                .findById(request.getStockTransactionId())
//                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
//
//        Stock stock = new Stock();
//        stock.setVariant(variant);
//        stock.setStockType(request.getStockType());
//        stock.setQuantity(request.getQuantity());
//
//        if (request.getStoreId() != null) {
//            Store store = storeRepository.findById(request.getStoreId())
//                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
//            stock.setStore(store);
//        }
//
//        if (request.getWarehouseId() != null) {
//            WareHouse warehouse = wareHouseRepository.findById(request.getWarehouseId())
//                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
//            stock.setWarehouse(warehouse);
//        }
//
//        stock = stockRepository.save(stock);
//        return stockMapper.toStockResponse(stock);
        StockTransaction stockTransaction = stockTransactionRepository
                .findById(request.getStockTransactionId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.STOCK_NOT_FOUND));
        StockResponse response= null;
        if (stockTransaction.getType() == TransactionType.IMPORT_TO_WAREHOUSE) {
            for (StockTransactionItemReceivedRequest receivedRequest
                    : request.getStockTransactionItemId()) {
                ProductVariant variant = productVariantRepository
                        .findById(receivedRequest.getStockTransactionId())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
                StockTransactionItem item = stockTransactionItemRepository
                        .findByVariantAndTransaction(variant, stockTransaction);
//                Optional<Stock> variantStock = stockRepository.findByVariantAndWarehouse
//                        (item.getVariant(), stockTransaction.getToWareHouse());
                Stock stock = Stock.builder()
                        .warehouse(stockTransaction.getToWareHouse())
                        .variant(item.getVariant())
                        .quantity(item.getQuantity())
                        .stockType(StockType.WAREHOUSE)
                        .store(null)
                        .build();
                Stock savedStockForWareHouse = stockRepository.save(stock);
                item.setOriginalTransactionItem(item);
              response=  stockMapper.toStockResponse(savedStockForWareHouse);
            }
        } else {
            List<StockTransactionItem> items = stockTransaction.getItems();
            for (StockTransactionItem item : items) {
                Stock stock = Stock.builder()
                        .warehouse(null)
                        .variant(item.getVariant())
                        .quantity(item.getQuantity())
                        .stockType(StockType.STORE)
                        .store(item.getTransaction().getToStore())
                        .build();
                Stock savedStockForStore = stockRepository.save(stock);
                item.setOriginalTransactionItem(item);
                response= stockMapper.toStockResponse(savedStockForStore);
            }
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