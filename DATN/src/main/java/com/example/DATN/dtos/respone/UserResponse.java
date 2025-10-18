package com.example.DATN.dtos.respone;

import com.example.DATN.Validator.DobConstraint;
import com.example.DATN.models.Order;
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
    @DobConstraint(min = 18,message = "INVALID_DOB")
    LocalDate dob;
    Set<RoleResponse> roles;
    List<Order> orders;
}
