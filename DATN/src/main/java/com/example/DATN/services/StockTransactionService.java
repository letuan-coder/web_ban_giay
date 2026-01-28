package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.StockType;
import com.example.DATN.constant.TransactionStatus;
import com.example.DATN.constant.TransactionType;
import com.example.DATN.dtos.request.*;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockTransactionService {
    private final ProductRepository productRepository;

    private final StockTransactionRepository transactionRepository;
    private final ProductVariantRepository variantRepository;
    private final StockRepository stockRepository;
    private final StoreRepository storeRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final WareHouseRepository wareHouseRepository;
    private final SupplierRepository supplierRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    public static final String IMPORT_TO_WAREHOUSE = "IMW";
    public static final String IMPORT_TO_STORE     = "IMS";
    public static final String EXPORT_TO_STORE     = "EXP";
    public static final String TRANSFER            = "TRF";
    public static final String RETURN_TO_SUPPLIER  = "RTS";
    public static final String RETURN_TO_WAREHOUSE = "RTW";
    public static final String ADJUST              = "ADJ";    private final ProductVariantRepository productVariantRepository;
    @Transactional
    public void createTransaction(StockTransactionRequest request) {
        LocalDate today = LocalDate.now();

        if (!request.getExpectedReceivedDate().isAfter(today)) {
            throw new ApplicationException(ErrorCode.INVALID_DATE);
        }

        StockTransaction transaction = new StockTransaction();
        transaction.setType(request.getType());

        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setItems(new ArrayList<>());
        StockTransaction savedTransaction=stockTransactionRepository.save(transaction);
        long code = snowflakeIdGenerator.nextId();
        switch (request.getType()) {
            case IMPORT_TO_STORE:
                String typeCode = IMPORT_TO_STORE + code;
                savedTransaction.setCode(typeCode);
                ImportStoreTransactionRequest importStoreTransactionRequest =
                        ImportStoreTransactionRequest.builder()
                                .type(TransactionType.IMPORT_TO_STORE)
                                .FromWareHouse(request.getFromWarehouseId())
                                .ToStoreId(request.getToStoreId())
                                .items(request.getItems())
                                .expectedReceivedDate(request.getExpectedReceivedDate())
                                .build();
                handleImportToStore(importStoreTransactionRequest, savedTransaction);
                break;
            case IMPORT_TO_WAREHOUSE:
                checkProductFromSupplier(request.getSupplierId(),request.getItems());
                String typeWarehouseCode = IMPORT_TO_WAREHOUSE+code;
                savedTransaction.setCode(typeWarehouseCode);
                ImportWareHouseTransactionRequest importWareHouseTransactionRequest =
                        ImportWareHouseTransactionRequest.builder()
                                .type(TransactionType.IMPORT_TO_WAREHOUSE)
                                .fromSupplierId(request.getSupplierId())
                                .toWarehouseId(request.getToWarehouseId())
                                .items(request.getItems())
                                .expectedReceivedDate(request.getExpectedReceivedDate())
                                .build();
                handleImportToWareHouse(importWareHouseTransactionRequest, savedTransaction);
                break;
            case TRANSFER:
                handleTransfer(request, savedTransaction);
                break;
            case EXPORT_TO_STORE:
                String exportCode = EXPORT_TO_STORE + code;
                savedTransaction.setCode(exportCode);
                ImportStoreTransactionRequest exportWarehouseTransactionRequest =
                        ImportStoreTransactionRequest.builder()
                                .type(TransactionType.IMPORT_TO_STORE)
                                .FromWareHouse(request.getFromWarehouseId())
                                .ToStoreId(request.getToStoreId())
                                .items(request.getItems())
                                .expectedReceivedDate(request.getExpectedReceivedDate())
                                .build();
                handleExportFromWareHouse(exportWarehouseTransactionRequest, savedTransaction);
                break;
            case RETURN_TO_WAREHOUSE:
                ReturnWareHouseTransactionRequest returnWareHouseTransactionRequest =
                        ReturnWareHouseTransactionRequest.builder()
                                .type(TransactionType.RETURN_TO_WAREHOUSE)
                                .FromStore(request.getFromStoreId())
                                .ToWareHouse(request.getToWarehouseId())
                                .items(request.getItems())
                                .expectedReceivedDate(request.getExpectedReceivedDate())
                                .build();
                handleReturnToWarehouse(returnWareHouseTransactionRequest, savedTransaction);
            case RETURN_TO_SUPPLIER:
                ReturnToSupplierTransactionRequest returnToSupplierTransactionRequest =
                        ReturnToSupplierTransactionRequest.builder()
                                .type(TransactionType.RETURN_TO_WAREHOUSE)
                                .fromWarehouse(request.getFromWarehouseId())
                                .toSupplier(request.getSupplierId())
                                .items(request.getItems())
                                .expectedReceivedDate(request.getExpectedReceivedDate())
                                .build();
                handleReturnToSupplier(returnToSupplierTransactionRequest, savedTransaction);
            default:
                throw new ApplicationException(ErrorCode.INVALID_VALIDATION);
        }

        for (var itemRequest : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
            StockTransactionItem transactionItem = StockTransactionItem.builder()
                    .transaction(savedTransaction)
                    .variant(variant)
                    .quantity(itemRequest.getQuantity())
                    .build();
            savedTransaction.getItems().add(transactionItem);
            log.info("Transaction id={}, type={}", savedTransaction.getId(), savedTransaction.getType());

            if (request.getType() == TransactionType.IMPORT_TO_STORE) {
                updateStock(savedTransaction.getToWareHouse(), savedTransaction.getToStore(),
                        variant, itemRequest.getQuantity(), true);
            } else if (request.getType() == TransactionType.TRANSFER) {
                updateStock(savedTransaction.getFromWareHouse(), savedTransaction.getFromStore(), variant, itemRequest.getQuantity(), false);
                updateStock(savedTransaction.getToWareHouse(), savedTransaction.getToStore(), variant, itemRequest.getQuantity(), true);
            }
        }
        transactionRepository.save(savedTransaction);
    }

    public Boolean checkProductFromSupplier(String supplierId,List<StockTransactionItemRequest> items){
        UUID supplierUUID = UUID.fromString(supplierId);
        Supplier supplier =supplierRepository.findById(supplierUUID)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        boolean flag = true;
        for (StockTransactionItemRequest item: items){
           flag = productRepository.existsBySupplier(supplier);
           if(flag==false){
               throw new ApplicationException(ErrorCode.PRODUCT_NOT_FROM_SUPPLIER);
           }
        }
        return flag;
    }
    private void handleReturnToSupplier(ReturnToSupplierTransactionRequest request,
                                        StockTransaction transaction) {
        if (request.getToSupplier() == null || (request.getFromWarehouse() == null)) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION);
        }
        Supplier supplier = supplierRepository.findBySupplierCode(request.getToSupplier())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));

        transaction.setSupplier(supplier);
        WareHouse wareHouse = wareHouseRepository.findBywarehouseCode(request.getFromWarehouse())
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        transaction.setToWareHouse(wareHouse);
    }

    private void handleReturnToWarehouse(ReturnWareHouseTransactionRequest request,
                                         StockTransaction transaction) {
        if (request.getFromStore() == null || (request.getToWareHouse() == null)) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION);
        }
        Store store = storeRepository.findByCode(request.getFromStore())
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        transaction.setFromStore(store);
        WareHouse wareHouse = wareHouseRepository.findBywarehouseCode(request.getToWareHouse())
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        transaction.setToWareHouse(wareHouse);
    }

    private void handleExportFromWareHouse(ImportStoreTransactionRequest request
            , StockTransaction transaction) {
        UUID storeId = UUID.fromString(request.getToStoreId());
        UUID warehouseId = UUID.fromString(request.getFromWareHouse());
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        WareHouse wareHouse = wareHouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
        transaction.setFromWareHouse(wareHouse);
        transaction.setToStore(store);
        List<StockTransactionItem> stockTransactionItems = transaction.getItems();
        for (StockTransactionItem item : stockTransactionItems) {
            item.setOriginalTransactionItem(item.getOriginalTransactionItem());
        }
    }

    private void handleImportToWareHouse(ImportWareHouseTransactionRequest request,
                                         StockTransaction transaction) {
        if (request.getFromSupplierId() == null || (request.getToWarehouseId() == null)) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION);
        }
        UUID id = UUID.fromString(request.getFromSupplierId());
        UUID wareId = UUID.fromString(request.getToWarehouseId());
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        transaction.setSupplier(supplier);
        WareHouse wareHouse = wareHouseRepository.findById(wareId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        transaction.setToWareHouse(wareHouse);
        List<StockTransactionItemRequest> transactionItems = request.getItems();

        for (StockTransactionItemRequest item : transactionItems) {
           addToStockTransaction(item,transaction);
        }
    }
    public StockTransactionItem addToStockTransaction(
            StockTransactionItemRequest request,StockTransaction transaction){
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND));
        StockTransactionItem item = StockTransactionItem.builder()
                .variant(variant)
                .transaction(transaction)
                .originalTransactionItem(null)
                .build();
        return item;
    }

//    @Transactional
//    public void createMissingItemsInvoice(CreateMissingItemsInvoiceRequest request) {
//        StockTransaction originalTransaction = transactionRepository.findById(request.getOriginalTransactionId())
//                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND, "Original transaction not found"));
//
//        if (originalTransaction.getType() != TransactionType.IMPORT) {
//            throw new ApplicationException(ErrorCode.INVALID_VALIDATION, "Can only create a missing items invoice for an IMPORT transaction.");
//        }
//
//        StockTransaction missingItemsInvoice = new StockTransaction();
//        missingItemsInvoice.setType(TransactionType.IMPORT);
//        missingItemsInvoice.setStatus(TransactionStatus.PENDING_COMPLETION);
//        missingItemsInvoice.setOriginalTransaction(originalTransaction);
//        missingItemsInvoice.setSupplier(originalTransaction.getSupplier());
//        missingItemsInvoice.setToWareHouse(originalTransaction.getToWareHouse());
//        missingItemsInvoice.setToStore(originalTransaction.getToStore());
//        missingItemsInvoice.setItems(new ArrayList<>());
//
//        for (MissingItemDTO itemDTO : request.getMissingItems()) {
//            ProductVariant variant = variantRepository.findById(itemDTO.getProductVariantId())
//                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
//
//            StockTransactionItem transactionItem = StockTransactionItem.builder()
//                    .transaction(missingItemsInvoice)
//                    .variant(variant)
//                    .quantity(itemDTO.getQuantity())
//                    .build();
//            missingItemsInvoice.getItems().add(transactionItem);
//        }
//        transactionRepository.save(missingItemsInvoice);
//    }

    @Transactional
    private void handleImportToStore(ImportStoreTransactionRequest request,
                                     StockTransaction transaction) {
        if (request.getFromWareHouse() == null || (request.getToStoreId() == null)) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION);
        }
        WareHouse wareHouse = wareHouseRepository.findBywarehouseCode(request.getFromWareHouse())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        transaction.setFromWareHouse(wareHouse);
        Store toStore = storeRepository.findByCode(request.getToStoreId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        transaction.setToStore(toStore);

    }

    @Transactional
    private void handleTransfer(StockTransactionRequest request, StockTransaction transaction) {
        if ((request.getFromStoreId() == null && request.getFromWarehouseId() == null) || (request.getToStoreId() == null && request.getToWarehouseId() == null)) {
            throw new ApplicationException(ErrorCode.INVALID_VALIDATION, "Transfer requires a source and a destination.");
        }

        if (request.getFromWarehouseId() != null) {
            WareHouse fromWareHouse = wareHouseRepository.findBywarehouseCode(request.getFromWarehouseId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
            transaction.setFromWareHouse(fromWareHouse);
        } else {
            Store fromStore = storeRepository.findByCode(request.getFromStoreId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
            transaction.setFromStore(fromStore);
        }

        // Set destination
        if (request.getToWarehouseId() != null) {
            WareHouse toWareHouse = wareHouseRepository.findBywarehouseCode(request.getToWarehouseId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.WAREHOUSE_NOT_FOUND));
            transaction.setToWareHouse(toWareHouse);
        } else {
            Store toStore = storeRepository.findByCode(request.getToStoreId())
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
            stock = stockRepository.findByVariant_IdAndWarehouse(variant.getId(), warehouse)
                    .orElseGet(() -> createNewStock(variant, warehouse, null));
        } else if (store != null) {
            stock = stockRepository.findByVariant_IdAndStore(variant.getId(), store)
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
                .stockType(warehouse != null ? StockType.WAREHOUSE : StockType.STORE)
                .quantity(0)
                .build();
    }

    @Transactional
    public List<StockTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public StockTransaction getTransactionById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND, "Transaction not found"));
    }

    public StockTransaction getTransactionByCode(String code) {
        return transactionRepository.findByCode(code)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND, "Transaction not found"));
    }
}
