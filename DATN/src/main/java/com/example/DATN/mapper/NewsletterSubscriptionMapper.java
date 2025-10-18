
package com.example.DATN.mapper;

import com.example.DATN.dtos.request.NewsletterSubscriptionRequest;
import com.example.DATN.dtos.respone.NewsletterSubscriptionResponse;
import com.example.DATN.models.NewsletterSubscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NewsletterSubscriptionMapper {
    NewsletterSubscriptionResponse toNewsletterSubscriptionResponse(NewsletterSubscription subscription);
    NewsletterSubscription toNewsletterSubscription(NewsletterSubscriptionRequest request);
}
