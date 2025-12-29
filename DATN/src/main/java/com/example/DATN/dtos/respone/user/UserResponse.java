package com.example.DATN.dtos.respone.user;

import com.example.DATN.dtos.respone.order.OrderResponse;
import com.example.DATN.dtos.respone.user_address.UserAddressResponse;
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
    List<UserAddressResponse> userAddress;
    Set<UserRoleResponse> roles;
    List<OrderResponse> orders;
}
