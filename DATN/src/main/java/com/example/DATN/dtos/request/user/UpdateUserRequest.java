package com.example.DATN.dtos.request.user;

import com.example.DATN.Validator.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateUserRequest {

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    String firstName;

    @NotBlank(message = "LAST_NAME_REQUIRED")
    String lastName;

    @Email(message = "INVALID_EMAIL")
    String email;

    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;

}

