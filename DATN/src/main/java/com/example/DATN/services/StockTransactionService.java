package com.example.DATN.services;

import com.example.DATN.constant.StockType;
import com.example.DATN.constant.TransactionType;
import com.example.DATN.dtos.request.StockTransactionRequest;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.StockTransactionMapper;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransactionService {

    private final StockTransactionRepository transactionRepository;
    private final ProductVariantRepository variantRepository;
    private final StockRepository stockRepository;
    private final StoreRepository storeRepository;
    private final WareHouseRepository wareHouseRepository;
    private final SupplierRepository supplierRepository;
    private final StockTransactionMapper transactionMapper;

    @Transactional
    public void createTransaction(StockTransactionRequest request) {
        StockTransaction transaction = new StockTransaction();
        transaction.setType(request.getType());
        transaction.setItems(new ArrayList<>());

        // Process based on transaction type
        switch (request.getType()) {
            case IMPORT:
                handleImport(request, transaction);
                break;
            case TRANSFER:
                handleTransfer(request, transaction);
                break;
            case EXPORT:
                // Logic for export (e.g., sales) can be added here
                throw new ApplicationException(ErrorCode.INVALID_VALIDATION, "Export functionality is not yet implemented.");
            default:
                throw new ApplicationException(ErrorCode.INVALID_VALIDATION, "Invalid transaction type.");
        }

        // Process items and update stock
        for (var itemRequest : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

            // Add item to transaction
            StockTransactionItem transactionItem = StockTransactionItem.builder()
                    .transaction(transaction)
                    .variant(variant)
                    .quantity(itemRequest.getQuantity())
                    .build();
            transaction.getItems().add(transactionItem);

            // Update stock based on type
            if (request.getType() == TransactionType.IMPORT) {
                updateStock(transaction.getToWareHouse(), transaction.getToStore(), variant, itemRequest.getQuantity(), true);
            } else if (request.getType() == TransactionType.TRANSFER) {
                // Decrease from source
                updateStock(transaction.getFromWareHouse(), transaction.getFromStore(), variant, itemRequest.getQuantity(), false);
                // Increase at destination
                updateStock(transaction.getToWareHouse(), transaction.getToStore(), variant, itemRequest.getQuantity(), true);
            }
        }

        transactionRepository.save(transaction);
    }

    @Transactional
    private void handleImport(StockTransactionRequest request, StockTransaction transaction) {
        if (request.getSupplierId() == null || (request.getToStoreId() == null && request.getToWarehouseId() == null)) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION, "Import requires a supplier and a destination (store or warehouse).");
        }
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED, "Supplier not found"));
        transaction.setSupplier(supplier);

        if (request.getToWarehouseId() != null) {
            WareHouse toWareHouse = wareHouseRepository.findById(request.getToWarehouseId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
            transaction.setToWareHouse(toWareHouse);
        } else {
            Store toStore = storeRepository.findById(request.getToStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            transaction.setToStore(toStore);
        }
    }

    @Transactional
    private void handleTransfer(StockTransactionRequest request, StockTransaction transaction) {
        if ((request.getFromStoreId() == null && request.getFromWarehouseId() == null) || (request.getToStoreId() == null && request.getToWarehouseId() == null)) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION, "Transfer requires a source and a destination.");
        }

        // Set source
        if (request.getFromWarehouseId() != null) {
            WareHouse fromWareHouse = wareHouseRepository.findById(request.getFromWarehouseId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
            transaction.setFromWareHouse(fromWareHouse);
        } else {
            Store fromStore = storeRepository.findById(request.getFromStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            transaction.setFromStore(fromStore);
        }

        // Set destination
        if (request.getToWarehouseId() != null) {
            WareHouse toWareHouse = wareHouseRepository.findById(request.getToWarehouseId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
            transaction.setToWareHouse(toWareHouse);
        } else {
            Store toStore = storeRepository.findById(request.getToStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            transaction.setToStore(toStore);
        }
    }

    @Transactional
    private void updateStock(WareHouse warehouse, Store store, ProductVariant variant, int quantity, boolean isIncrease) {
        Stock stock;
        if (warehouse != null) {
            stock = stockRepository.findByVariantAndWarehouse(variant, warehouse)
                    .orElseGet(() -> createNewStock(variant, warehouse, null));
        } else if (store != null) {
            stock = stockRepository.findByVariantAndStore(variant, store)
                    .orElseGet(() -> createNewStock(variant, null, store));
        } else {
            return;
        }

        if (isIncrease) {
            stock.setQuantity(stock.getQuantity() + quantity);
        } else {
            if (stock.getQuantity() < quantity) {
                throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK, "Insufficient stock for variant " + variant.getSku());
            }
            stock.setQuantity(stock.getQuantity() - quantity);
        }
        stockRepository.save(stock);
    }

    @Transactional
    private Stock createNewStock(ProductVariant variant, WareHouse warehouse, Store store) {
        return Stock.builder()
                .variant(variant)
                .warehouse(warehouse)
                .store(store)
                .stockType(warehouse != null ? StockType.WAREHOUSE :StockType.STORE)
                .quantity(0)
                .build();
    }

    @Transactional
    public List<StockTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional
    public StockTransaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND, "Transaction not found"));
    }
}
