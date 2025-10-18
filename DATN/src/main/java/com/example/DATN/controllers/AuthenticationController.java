package com.example.DATN.controllers;

import com.example.DATN.dtos.request.AuthenticationRequest;
import com.example.DATN.dtos.request.IntrospectRequest;
import com.example.DATN.dtos.request.LogoutRequest;
import com.example.DATN.dtos.request.RefreshRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.AuthenticationResponse;
import com.example.DATN.dtos.respone.IntrospectResponse;
import com.example.DATN.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RequestMapping("/api/auth")
@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthenticationController {
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse <AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse isSuccess = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(isSuccess)
                .build();
    }
    @PostMapping("/introspect")
    ApiResponse<Boolean> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        IntrospectResponse isSuccess = authenticationService.Introspect(request);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authenticated? {}", authentication.isAuthenticated());
        log.info("Authorities: {}", authentication.getAuthorities());
        return ApiResponse.<Boolean>builder()
                .data(isSuccess.isActive())
                .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .message("logout thành công")
                .build();
    }
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> logout(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result= authenticationService.RefreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .message(result.getMessage())
                .data(result)
                .build();
    }

}
