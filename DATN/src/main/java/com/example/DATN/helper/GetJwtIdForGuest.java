package com.example.DATN.helper;

import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class GetJwtIdForGuest {
    public String GetGuestKey() {
        if(SecurityContextHolder.getContext().getAuthentication() == null){
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String guest_key= "guest:"+jwt.getId();
        return guest_key;
    }

}
