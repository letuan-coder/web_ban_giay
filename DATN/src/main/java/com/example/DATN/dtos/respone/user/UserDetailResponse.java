package com.example.DATN.dtos.respone.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserDetailResponse {
    Long id;
    String username;
    String firstName;
    String lastName;
    LocalDate dob;
    String email;
    String userImage;
}
