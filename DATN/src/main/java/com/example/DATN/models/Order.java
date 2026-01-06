package com.example.DATN.models;

import com.example.DATN.constant.OrderStatus;
import com.example.DATN.constant.PaymentMethodEnum;
import com.example.DATN.constant.PaymentStatus;
import com.example.DATN.constant.ShippingStatus;
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

    private String orderCode;
    // Mã đơn hàng GHN
    private String ghnOrderCode;

    // Mã phân loại nội bộ của GHN
    private String sortCode;


    // Loại vận chuyển (truck, motor…)
    private String transType;

    // Mã phường/xã
    private String wardEncode;

    // Mã quận/huyện
    private String districtEncode;

    // Tổng phí GHN (total_fee)
    private Integer totalFee;

    // Phí dịch vụ chính (main_service)
    private Integer mainServiceFee;

    // Thời gian dự kiến giao hàng
    private LocalDateTime expectedDeliveryTime;

    // Đối tác vận hành (operation_partner)
    private String operationPartner;

    // Thời gian cập nhật trạng thái lần cuối
    private LocalDateTime ghnLastUpdated;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-orders")
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonManagedReference("order_items")
    private List<OrderItem> items;

    private Integer serviceId;

    private PaymentMethodEnum paymentMethod;

    private BigDecimal shippingFee;

    private LocalDateTime receivedDate;

    @NotNull
    private BigDecimal total_price;

    private String Note;

    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @Embedded
    ShippingAddress userAddresses;

    Integer total_weight;
    Integer total_height;
    Integer total_width;
    Integer total_length;
    @Enumerated(EnumType.STRING)
    ShippingStatus ghnStatus;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("orders-return")
    private List<OrderReturn> returns;
}