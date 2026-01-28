package com.example.DATN.dtos.request.user;

import com.example.DATN.Validator.DobConstraint;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    @Column(unique = true)
    String username;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String passwordConfirm;

    @DobConstraint(min = 12,message = "INVALID_DOB")
    LocalDate dob;

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    String firstName;
    @NotBlank(message = "LAST_NAME_REQUIRED")
    String lastName;

    @Email(message = "INVALID_EMAIL")
    String email;

    Set<String> roles;
}
