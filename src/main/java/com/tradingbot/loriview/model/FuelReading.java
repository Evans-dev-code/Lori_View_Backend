package com.tradingbot.loriview.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "fuel_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private ZonedDateTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(name = "fuel_level_l", precision = 8, scale = 2)
    private BigDecimal fuelLevelL;     // current fuel level in litres

    @Column(name = "fuel_rate_lph", precision = 6, scale = 2)
    private BigDecimal fuelRateLph;    // consumption rate litres per hour
}