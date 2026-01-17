package com.example.DATN.repositories.projection;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckOutProjection {
    private UUID storeId;
    @Builder.Default
    private double distanceKm =0.0;

}
