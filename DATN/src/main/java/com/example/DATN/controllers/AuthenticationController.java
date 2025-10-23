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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RequestMapping("/api/auth")
@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Value("${cookie.duration.days}")
    private Integer COOKIE_DURATION;
    @PostMapping("/login")
    ApiResponse <AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request,
            @RequestParam(value="remember-me", required = false, defaultValue = "false")
            Boolean remember,
            HttpServletResponse response) {
        AuthenticationResponse authResponse = authenticationService.authenticate(request);
        if (remember) {
            Cookie cookie = new Cookie("remember-me", authResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // nếu dùng HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(COOKIE_DURATION); // 7 ngày
            response.addCookie(cookie);
        }
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
    ApiResponse<AuthenticationResponse> logout(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result= authenticationService.RefreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .message(result.getMessage())
                .data(result)
                .build();
    }

}
