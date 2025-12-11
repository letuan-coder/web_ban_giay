package com.example.DATN.dtos.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdatePasswordRequest {
    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 4, message = "USERNAME_MIN_LENGTH")
    String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "PASSWORD_MIN_LENGTH")
    String OldPassword;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "PASSWORD_MIN_LENGTH")
    String password;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "PASSWORD_MIN_LENGTH")
    String ConfirmPassword;
}

