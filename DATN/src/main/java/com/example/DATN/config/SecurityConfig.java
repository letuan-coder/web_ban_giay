package com.example.DATN.config;

import com.example.DATN.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
   Role role;
    private final String[] PUBLIC_API =
            {"/api/users/register",
                    "/api/auth/**"
                    ,"/api/permission"
                    ,"/api/role"
                    ,"/api/auth/logout"
                    ,"/api/auth/introspect"
                    ,"/api/auth/refresh"
                    ,"/api/images/view/**"
                    ,"/api/newsletter/subscribe"
                    ,"/api/products"
                    ,"/api/products/search"
                    ,"/api/ghtk/create-order",
                    "/api/guest"};
    private final String [] POST_PUBLIC_API={"/api/ghtk/create-order"};
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private CustomeJwtDecoder customeJwtDecoder;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.authorizeHttpRequests(request -> request
                .requestMatchers( PUBLIC_API).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users")
                .hasAuthority("USER_VIEW")
                .requestMatchers(HttpMethod.GET,"/vnpay/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/products").permitAll()
                .requestMatchers(HttpMethod.POST,POST_PUBLIC_API).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/brands/**", "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/provinces/**","/api/districts/**","/api/communes/**").permitAll()
                .anyRequest().authenticated()
        );
        httpSecurity.oauth2ResourceServer(oauth2->
                oauth2.jwt(jwtConfigurer->jwtConfigurer
                                .decoder(customeJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("scope"); // lấy từ claim scope
        authoritiesConverter.setAuthorityPrefix("");       // đổi prefix thành ROLE_

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
