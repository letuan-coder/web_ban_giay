package com.example.DATN.services;

import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentUser {
    private final UserRepository userRepository;
    public User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = jwt.getSubject(); // "admin"

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
    }
}
