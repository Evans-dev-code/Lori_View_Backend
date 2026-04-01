package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllByOrderByCreatedAtDesc();
    // Get the current active subscription for an owner
    Optional<Subscription> findByOwnerIdAndIsActiveTrue(Long ownerId);

    // All subscriptions for an owner (history)
    List<Subscription> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // Admin — all active subscriptions across platform
    List<Subscription> findByIsActiveTrueOrderByCreatedAtDesc();

    // Count active subscribers — useful for admin dashboard
    long countByIsActiveTrue();

    // Check if owner has any active subscription
    boolean existsByOwnerIdAndIsActiveTrue(Long ownerId);
}