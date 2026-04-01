package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.enums.SubscriptionPlan;
import com.tradingbot.loriview.model.Subscription;
import com.tradingbot.loriview.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // POST /api/v1/subscriptions/create
    @PostMapping("/create")
    public ResponseEntity<Subscription> createSubscription(
            @RequestParam Long ownerId,
            @RequestParam String plan) {
        SubscriptionPlan subscriptionPlan =
                SubscriptionPlan.valueOf(plan.toUpperCase());
        return ResponseEntity.ok(
                subscriptionService.createSubscription(ownerId, subscriptionPlan)
        );
    }

    // GET /api/v1/subscriptions/owner/{ownerId}/active
    @GetMapping("/owner/{ownerId}/active")
    public ResponseEntity<Subscription> getActiveSubscription(
            @PathVariable Long ownerId) {
        Optional<Subscription> sub =
                subscriptionService.getActiveSubscription(ownerId);
        return sub.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/subscriptions/owner/{ownerId}/history
    @GetMapping("/owner/{ownerId}/history")
    public ResponseEntity<List<Subscription>> getHistory(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(
                subscriptionService.getSubscriptionHistory(ownerId)
        );
    }
}