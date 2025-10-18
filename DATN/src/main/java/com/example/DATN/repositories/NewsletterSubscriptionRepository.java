
package com.example.DATN.repositories;

import com.example.DATN.models.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, String> {
}
