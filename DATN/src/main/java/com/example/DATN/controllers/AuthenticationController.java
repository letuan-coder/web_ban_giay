package com.example.DATN.controllers;

import com.example.DATN.dtos.request.jwt.*;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.jwt.AuthenticationResponse;
import com.example.DATN.dtos.respone.jwt.IntrospectResponse;
import com.example.DATN.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final AuthenticationService authenticationService;


    @PostMapping("/login")
    ApiResponse <AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request ){
        AuthenticationResponse authResponse = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .data(authResponse)
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
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result= authenticationService.RefreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .message(result.getMessage())
                .data(result)
                .build();
    }
    @PostMapping("/guest")
    ApiResponse<AuthenticationResponse> createGuest() {
        AuthenticationResponse authResponse = authenticationService
                .createGuestAndAuthenticate();
        return ApiResponse.<AuthenticationResponse>builder()
                .data(authResponse)
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        authenticationService.sendResetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .data(null)
                        .message("Password reset instructions have been sent to your email")
                        .build()
        );
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody ResetPasswordRequest request) throws ParseException, JOSEException {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .data(null)
                        .message("Your password has been successfully reset")
                        .build()
        );
    }

    @PostMapping("/google")
    public ApiResponse<AuthenticationResponse> googleLogin(
            @RequestBody GoogleLoginRequest request) {
        AuthenticationResponse authResponse = authenticationService.loginWithGoogle(request.getToken());
        return ApiResponse.<AuthenticationResponse>builder()
                .data(authResponse)
                .build();
    }
}
