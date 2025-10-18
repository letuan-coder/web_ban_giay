package com.example.DATN.controllers;

import com.example.DATN.dtos.request.NewsletterSubscriptionRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.NewsletterSubscriptionResponse;
import com.example.DATN.services.NewsletterSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterSubscriptionController {

    private final NewsletterSubscriptionService newsletterSubscriptionService;

    @PostMapping("/subscribe")
    public ApiResponse<NewsletterSubscriptionResponse> subscribe(@RequestBody @Valid NewsletterSubscriptionRequest request) {
        return ApiResponse.<NewsletterSubscriptionResponse>builder()
                .data(newsletterSubscriptionService.subscribe(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<NewsletterSubscriptionResponse>> getAllSubscriptions() {
        return ApiResponse.<List<NewsletterSubscriptionResponse>>builder()
                .data(newsletterSubscriptionService.getAllSubscriptions())
                .build();
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NewsletterSubscriptionResponse> getSubscriptionByEmail(@PathVariable String email) {
        return ApiResponse.<NewsletterSubscriptionResponse>builder()
                .data(newsletterSubscriptionService.getSubscriptionByEmail(email))
                .build();
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unsubscribe(@PathVariable String email) {
        newsletterSubscriptionService.unsubscribe(email);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/reactivate/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NewsletterSubscriptionResponse> reactivateSubscription(@PathVariable String email) {
        return ApiResponse.<NewsletterSubscriptionResponse>builder()
                .data(newsletterSubscriptionService.reactivate(email))
                .build();
    }
}