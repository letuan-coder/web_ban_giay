package com.example.DATN.helper;

import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetUserByJwtHelper {
    private final UserRepository userRepository;
    private final RedisTemplate redisTemplate;

    public User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = jwt.getSubject();
        return userRepository.findByUsername(username)
                .orElseThrow(()->new ApplicationException(ErrorCode.USER_NOT_EXISTED));

    }
}

