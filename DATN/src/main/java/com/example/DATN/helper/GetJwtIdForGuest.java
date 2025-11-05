package com.example.DATN.helper;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class GetJwtIdForGuest {
    public String GetGuestKey() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String guest_key= "guest:"+jwt.getId();
        return guest_key;
    }

}
