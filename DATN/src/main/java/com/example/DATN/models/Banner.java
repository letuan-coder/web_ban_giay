package com.example.DATN.models;

import com.example.DATN.constant.BannerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "banners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Banner extends BaseEntity {
    @Id
    @UuidGenerator
    UUID id;

    String bannerName;

    @NotNull
    private String imageUrl;

    private String redirectUrl;

    private Integer sortOrder;

    private Boolean active = true;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private BannerType type;
}
