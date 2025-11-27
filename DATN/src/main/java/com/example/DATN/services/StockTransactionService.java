package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.StockType;
import com.example.DATN.constant.TransactionStatus;
import com.example.DATN.constant.TransactionType;
import com.example.DATN.dtos.request.CreateMissingItemsInvoiceRequest;
import com.example.DATN.dtos.request.MissingItemDTO;
import com.example.DATN.dtos.request.StockTransactionRequest;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockTransactionService {

    private final StockTransactionRepository transactionRepository;
    private final ProductVariantRepository variantRepository;
    private final StockRepository stockRepository;
    private final StoreRepository storeRepository;
    private final WareHouseRepository wareHouseRepository;
    private final SupplierRepository supplierRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    @Transactional
    public void createTransaction(StockTransactionRequest request) {
        StockTransaction transaction = new StockTransaction();
        Long newId = snowflakeIdGenerator.nextId();
        transaction.setId(newId);
        transaction.setType(request.getType());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setItems(new ArrayList<>());
        switch (request.getType()) {
            case IMPORT:
                handleImport(request, transaction);
                break;
            case TRANSFER:
                handleTransfer(request, transaction);
                break;
            case EXPORT:
                throw new ApplicationException(ErrorCode.INVALID_VALIDATION,"INVALID EXPORT");
            default:
                throw new ApplicationException(ErrorCode.INVALID_VALIDATION);
        }

        for (var itemRequest : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
            StockTransactionItem transactionItem = StockTransactionItem.builder()
                    .transaction(transaction)
                    .variant(variant)
                    .quantity(itemRequest.getQuantity())
                    .build();
            transaction.getItems().add(transactionItem);
            log.info("Transaction id={}, type={}", transaction.getId(), transaction.getType());

            if (request.getType() == TransactionType.IMPORT) {
                updateStock(transaction.getToWareHouse(), transaction.getToStore(),
                        variant, itemRequest.getQuantity(), true);
            } else if (request.getType() == TransactionType.TRANSFER) {
                updateStock(transaction.getFromWareHouse(), transaction.getFromStore(), variant, itemRequest.getQuantity(), false);
                updateStock(transaction.getToWareHouse(), transaction.getToStore(), variant, itemRequest.getQuantity(), true);
            }
        }
        transactionRepository.save(transaction);
    }

    @Transactional
    public void createMissingItemsInvoice(CreateMissingItemsInvoiceRequest request) {
        StockTransaction originalTransaction = transactionRepository.findById(request.getOriginalTransactionId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND, "Original transaction not found"));

        if (originalTransaction.getType() != TransactionType.IMPORT) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION, "Can only create a missing items invoice for an IMPORT transaction.");
        }

        StockTransaction missingItemsInvoice = new StockTransaction();
        missingItemsInvoice.setType(TransactionType.IMPORT);
        missingItemsInvoice.setStatus(TransactionStatus.PENDING_COMPLETION);
        missingItemsInvoice.setOriginalTransaction(originalTransaction);
        missingItemsInvoice.setSupplier(originalTransaction.getSupplier());
        missingItemsInvoice.setToWareHouse(originalTransaction.getToWareHouse());
        missingItemsInvoice.setToStore(originalTransaction.getToStore());
        missingItemsInvoice.setItems(new ArrayList<>());

        for (MissingItemDTO itemDTO : request.getMissingItems()) {
            ProductVariant variant = variantRepository.findById(itemDTO.getProductVariantId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

            StockTransactionItem transactionItem = StockTransactionItem.builder()
                    .transaction(missingItemsInvoice)
                    .variant(variant)
                    .quantity(itemDTO.getQuantity())
                    .build();
            missingItemsInvoice.getItems().add(transactionItem);
        }

        transactionRepository.save(missingItemsInvoice);
    }

    @Transactional
    private void handleImport(StockTransactionRequest request, StockTransaction transaction) {
        if (request.getSupplierId() == null || (request.getToStoreId() == null
                && request.getToWarehouseId() == null)) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION);
        }
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
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
    private void updateStock(
            WareHouse warehouse, Store store,
            ProductVariant variant,
            Integer quantity, Boolean isIncrease) {
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
