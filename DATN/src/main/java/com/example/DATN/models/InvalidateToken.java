package com.example.DATN.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "InvalidateToken")
public class InvalidateToken {
    @Id
    String id;
    Date expiryTime;
}
