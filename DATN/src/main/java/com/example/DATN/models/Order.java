package com.example.DATN.models;

import com.example.DATN.constant.OrderStatus;
import com.example.DATN.constant.PaymentMethodEnum;
import com.example.DATN.constant.PaymentStatus;
import com.example.DATN.constant.ShippingStatus;
import com.example.DATN.models.Embeddable.GHN;
import com.example.DATN.models.Embeddable.ShippingAddress;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(name = "order_code", nullable = false)
    private String orderCode;
    @Embedded
    GHN ghn;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-orders")
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonManagedReference("order_items")
    private List<OrderItem> items;
    @Column(name = "service_id")

    private Integer serviceId;
    @Column(name = "payment_method")

    private PaymentMethodEnum paymentMethod;
    @Column(name = "shipping_fee")

    private BigDecimal shippingFee;
    @Column(name = "received_date")

    private LocalDateTime receivedDate;

    @NotNull
    private BigDecimal total_price;

    @Column(name = "final_price")
    private BigDecimal finalPrice;


    private String Note;

    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @Embedded
    ShippingAddress userAddresses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store storeDelivered;

    Integer total_weight;
    Integer total_height;
    Integer total_width;
    Integer total_length;
    @Enumerated(EnumType.STRING)
    ShippingStatus ghnStatus;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    BigDecimal discountAmount;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonManagedReference("orders-return")
    private List<OrderReturn> returns;
}