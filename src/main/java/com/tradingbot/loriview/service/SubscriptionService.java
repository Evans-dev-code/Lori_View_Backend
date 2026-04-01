package com.tradingbot.loriview.service;

import com.tradingbot.loriview.enums.SubscriptionPlan;
import com.tradingbot.loriview.model.Owner;
import com.tradingbot.loriview.model.Subscription;
import com.tradingbot.loriview.repository.OwnerRepository;
import com.tradingbot.loriview.repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final OwnerRepository ownerRepository;

    // Subscription pricing in KES
    public static final BigDecimal BASIC_PRICE    = new BigDecimal("1500");
    public static final BigDecimal STANDARD_PRICE = new BigDecimal("3500");
    public static final BigDecimal PREMIUM_PRICE  = new BigDecimal("7000");

    // Create a new pending subscription (before payment)
    @Transactional
    public Subscription createSubscription(Long ownerId, SubscriptionPlan plan) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Owner not found: " + ownerId));

        BigDecimal amount = getPlanPrice(plan);

        Subscription subscription = Subscription.builder()
                .owner(owner)
                .plan(plan)
                .amountKes(amount)
                .isActive(false) // becomes true after M-Pesa confirms payment
                .build();

        return subscriptionRepository.save(subscription);
    }

    // Called by MpesaService after successful payment callback
    @Transactional
    public Subscription activateSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Subscription not found: " + subscriptionId));

        // Deactivate any previous active subscription
        subscriptionRepository
                .findByOwnerIdAndIsActiveTrue(subscription.getOwner().getId())
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    subscriptionRepository.save(existing);
                });

        // Activate this subscription for 30 days
        subscription.setIsActive(true);
        subscription.setStartedAt(ZonedDateTime.now());
        subscription.setExpiresAt(ZonedDateTime.now().plusDays(30));

        log.info("Subscription activated | owner: {} | plan: {}",
                subscription.getOwner().getId(), subscription.getPlan());

        return subscriptionRepository.save(subscription);
    }

    // Check if owner has an active subscription
    public boolean hasActiveSubscription(Long ownerId) {
        return subscriptionRepository.existsByOwnerIdAndIsActiveTrue(ownerId);
    }

    // Get current active subscription for an owner
    public Optional<Subscription> getActiveSubscription(Long ownerId) {
        return subscriptionRepository.findByOwnerIdAndIsActiveTrue(ownerId);
    }

    // Get subscription history for an owner
    public List<Subscription> getSubscriptionHistory(Long ownerId) {
        return subscriptionRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    // Get price for a plan
    public BigDecimal getPlanPrice(SubscriptionPlan plan) {
        return switch (plan) {
            case BASIC    -> BASIC_PRICE;
            case STANDARD -> STANDARD_PRICE;
            case PREMIUM  -> PREMIUM_PRICE;
        };
    }

    // Admin — all active subscriptions
    public List<Subscription> getAllActiveSubscriptions() {
        return subscriptionRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    // Admin — count active subscribers
    public long countActiveSubscribers() {
        return subscriptionRepository.countByIsActiveTrue();
    }
}