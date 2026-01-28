package com.example.DATN.repositories;

import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.StockTransaction;
import com.example.DATN.models.StockTransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransactionItemRepository extends JpaRepository<StockTransactionItem, Long> {
  StockTransactionItem findByVariantAndTransaction(ProductVariant variant, StockTransaction transaction);
}