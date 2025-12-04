package com.example.DATN.models;

import com.example.DATN.constant.TransactionStatus;
import com.example.DATN.constant.TransactionType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "stock_transaction")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockTransaction extends BaseEntity{
    @Id
    @UuidGenerator
    private UUID id;
    private String code; // mã OUT, IN, DEBT...

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "from_warehouse_id")
    private WareHouse fromWareHouse;

    @ManyToOne
    @JoinColumn(name = "from_store_id")
    private Store fromStore;

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
