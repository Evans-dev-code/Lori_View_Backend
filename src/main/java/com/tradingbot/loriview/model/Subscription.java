package com.tradingbot.loriview.model;

import com.tradingbot.loriview.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionPlan plan;

    @Column(name = "amount_kes", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountKes;       // price in Kenyan Shillings

    @Column(name = "started_at")
    private ZonedDateTime startedAt;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = false;  // becomes true after payment confirmed

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}