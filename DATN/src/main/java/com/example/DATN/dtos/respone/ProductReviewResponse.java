package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewResponse {
    String username;
    Integer size;
    String color;
    Integer rating;
    String comment;
    LocalDate createdAt;
}
