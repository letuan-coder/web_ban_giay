package com.example.DATN.dtos.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductReviewRequest {
    UUID orderItemId;
    Integer rating;
    String comment;
}
