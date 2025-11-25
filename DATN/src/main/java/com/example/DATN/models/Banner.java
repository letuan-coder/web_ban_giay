package com.example.DATN.models;

import com.example.DATN.constant.BannerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "banners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Banner  {
    @Id
    @UuidGenerator
    UUID id;

    String bannerName;

    @NotNull
    private String imageUrl;

    private String redirectUrl;

    private Integer sortOrder;

    private Boolean active = true;

    private LocalDate startAt;
    private LocalDate endAt;

    @Enumerated(EnumType.STRING)
    private BannerType type;
}
