package com.example.DATN.dtos.respone.user;

import com.example.DATN.dtos.respone.order.OrderRespone;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String firstName;
    String lastName;
    LocalDate dob;
    Set<UserRoleResponse> roles;
    List<OrderRespone> orders;
}
