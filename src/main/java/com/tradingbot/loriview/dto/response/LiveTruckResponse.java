package com.tradingbot.loriview.dto.response;

import com.tradingbot.loriview.enums.TruckStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveTruckResponse {

    private Long truckId;
    private String plateNumber;
    private String make;
    private String model;
    private TruckStatus status;

    // Live location
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speedKmh;
    private BigDecimal headingDeg;
    private ZonedDateTime lastSeen;

    // Live fuel
    private BigDecimal fuelLevelL;
    private BigDecimal fuelCapacityL;
    private BigDecimal fuelPercentage;

    // Current trip
    private Long currentTripId;
    private String currentDestination;
}