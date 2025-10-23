
package com.example.DATN.services;

import com.example.DATN.dtos.request.NewsletterSubscriptionRequest;
import com.example.DATN.dtos.respone.NewsletterSubscriptionResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.NewsletterSubscriptionMapper;
import com.example.DATN.models.NewsletterSubscription;
import com.example.DATN.repositories.NewsletterSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsletterSubscriptionService {

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final NewsletterSubscriptionMapper newsletterSubscriptionMapper;

    public NewsletterSubscriptionResponse subscribe(NewsletterSubscriptionRequest request) {
        if (newsletterSubscriptionRepository.findById(request.getEmail()).isPresent()) {
            throw new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        NewsletterSubscription subscription = newsletterSubscriptionMapper.toNewsletterSubscription(request);
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setActive(true); // New subscriptions are active
        subscription = newsletterSubscriptionRepository.save(subscription);
        return newsletterSubscriptionMapper.toNewsletterSubscriptionResponse(subscription);
    }

    public List<NewsletterSubscriptionResponse> getAllSubscriptions() {
        return newsletterSubscriptionRepository.findAll().stream()
                .map(newsletterSubscriptionMapper::toNewsletterSubscriptionResponse)
                .collect(Collectors.toList());
    }

    public NewsletterSubscriptionResponse getSubscriptionByEmail(String email) {
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findById(email)
                .orElseThrow(() -> new ApplicationException(ErrorCode.NEWSLETTER_SUBSCRIPTION_NOT_FOUND));
        return newsletterSubscriptionMapper.toNewsletterSubscriptionResponse(subscription);
    }

    public void unsubscribe(String email) {
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findById(email)
                .orElseThrow(() -> new ApplicationException(ErrorCode.NEWSLETTER_SUBSCRIPTION_NOT_FOUND));
        // Instead of deleting, we can set isActive to false
        subscription.setActive(false);
        newsletterSubscriptionRepository.save(subscription);
    }

    // Optional: Method to reactivate a subscription
    public NewsletterSubscriptionResponse reactivate(String email) {
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findById(email)
                .orElseThrow(() -> new ApplicationException(ErrorCode.NEWSLETTER_SUBSCRIPTION_NOT_FOUND));
        subscription.setActive(true);
        subscription = newsletterSubscriptionRepository.save(subscription);
        return newsletterSubscriptionMapper.toNewsletterSubscriptionResponse(subscription);
    }
}
