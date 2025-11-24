package com.example.DATN.models;

import com.example.DATN.constant.TransactionType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "stock_transaction")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockTransaction extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // For TRANSFERS or internal movements
    @ManyToOne
    @JoinColumn(name = "from_warehouse_id")
    private WareHouse fromWareHouse;

    @ManyToOne
    @JoinColumn(name = "from_store_id")
    private Store fromStore;

    // Destination of the transaction
    @ManyToOne
    @JoinColumn(name = "to_warehouse_id")
    private WareHouse toWareHouse;

    @ManyToOne
    @JoinColumn(name = "to_store_id")
    private Store toStore;

    @OneToMany(mappedBy = "transaction",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonManagedReference
    private List<StockTransactionItem> items = new ArrayList<>();
}
