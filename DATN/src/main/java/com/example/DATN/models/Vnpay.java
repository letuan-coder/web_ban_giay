package com.example.DATN.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "VnPay")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vnpay {
    @Id
    @UuidGenerator
    UUID id;
    @Column(name = "vnp_txn_ref")
    String vnpTxnRef;;
    String vnp_OrderInfo;
    String vnp_Amount;
    String vnp_ResponseCode;
    String vnp_TransactionNo;
    String vnp_BankCode;
    String vnp_PayDate;
}
