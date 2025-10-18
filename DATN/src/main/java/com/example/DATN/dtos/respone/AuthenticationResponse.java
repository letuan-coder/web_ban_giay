package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthenticationResponse {
    Boolean success;
    String message;
    String token;
}
