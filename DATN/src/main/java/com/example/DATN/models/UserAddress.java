
package com.example.DATN.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_addresses")
public class UserAddress extends BaseEntity{
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-address")
    private User user;

    @Column(nullable = false, length = 255)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String phoneNumber;
    
    private String provinceCode;

    private Integer districtCode;

    private String wardCode;
    private String provinceName;

    private String districtName;

    private String wardName;
    @Column(nullable = false, length = 500)
    private String streetDetail;

    private String userAddress;

    @Column(nullable = false)
    private boolean isDefault;

}
