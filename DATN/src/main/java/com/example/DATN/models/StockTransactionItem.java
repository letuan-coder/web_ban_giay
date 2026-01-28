package com.example.DATN.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_transaction_items")
public class StockTransactionItem extends BaseEntity {
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    @JsonBackReference
    private StockTransaction transaction;

    @ManyToOne
    @JoinColumn(name = "original_item_id")

    private StockTransactionItem originalTransactionItem;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;
    @JoinColumn(name = "received_quantity")
    private Integer receivedQuantity;
    private Integer quantity;
}
