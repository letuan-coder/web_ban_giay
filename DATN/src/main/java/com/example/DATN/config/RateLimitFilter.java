//package com.example.DATN.config;
//
//import io.github.bucket4j.Bandwidth;
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.Refill;
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.time.Duration;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class RateLimitFilter
//        implements Filter {
//    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest req = (HttpServletRequest) request;
//
//        // ⭐ Lấy Authentication từ SecurityContext
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        // ⭐ Nếu là ADMIN → bỏ qua rate limit
//        if (auth != null && auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
//
//            chain.doFilter(request, response);
//            return;
//        }
//
//        // ⭐ Nếu không có token hoặc không phải admin → rate limit theo IP
//        String ip = req.getRemoteAddr();
//        Bucket bucket = cache.computeIfAbsent(ip, this::newBucket);
//
//        if (bucket.tryConsume(1)) {
//            chain.doFilter(request, response);
//        } else {
//            HttpServletResponse res = (HttpServletResponse) response;
//            res.setStatus(429);
//            res.getWriter().write("Rate limit exceeded, try again later.");
//        }
//    }
//
//    private Bucket newBucket(String key) {
//        // 100 yêu cầu mỗi 1 phút
//        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
//        return Bucket.builder().addLimit(limit).build();
//    }
//}
