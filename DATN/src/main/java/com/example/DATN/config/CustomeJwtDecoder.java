package com.example.DATN.config;

import com.example.DATN.dtos.request.IntrospectRequest;
import com.example.DATN.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component

public class CustomeJwtDecoder implements JwtDecoder {
    @Value("${jwt.secret}")
    private String signerKey;

    @Autowired
    private AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder;

    public CustomeJwtDecoder(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var response = authenticationService.Introspect(
                    IntrospectRequest
                            .builder()
                            .token(token)
                            .build());
            if (!response.isActive())
                throw new JwtException("Token invalid");
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }
        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    signerKey.getBytes(),
                    "HS512"
            );
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }
        return nimbusJwtDecoder.decode(token);
    }
}
