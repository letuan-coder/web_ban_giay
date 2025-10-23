package com.example.DATN.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "color")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Color extends BaseEntity {
    @Id
    @Column(length = 10)
    String code; // ví dụ COL08

    @Column(nullable = false)
    String name; // tên màu, ví dụ "White"

    @Column(nullable = false)
    String hexCode;
}
