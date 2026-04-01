package com.tradingbot.loriview.model;

import com.tradingbot.loriview.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(length = 120)
    private String origin;         // e.g. Mombasa Port

    @Column(length = 120)
    private String destination;    // e.g. Kampala ICD

    @Column(name = "started_at")
    private ZonedDateTime startedAt;

    @Column(name = "ended_at")
    private ZonedDateTime endedAt;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "fuel_used_l", precision = 10, scale = 2)
    private BigDecimal fuelUsedL;

    @Column(name = "avg_speed_kmh", precision = 6, scale = 2)
    private BigDecimal avgSpeedKmh;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TripStatus status = TripStatus.ONGOING;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}