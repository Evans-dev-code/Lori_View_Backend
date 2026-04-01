package com.tradingbot.loriview.dto.response;

import com.tradingbot.loriview.enums.TripStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {

    private Long id;
    private Long truckId;
    private String plateNumber;
    private Long driverId;
    private String driverName;
    private String origin;
    private String destination;
    private ZonedDateTime startedAt;
    private ZonedDateTime endedAt;
    private BigDecimal distanceKm;
    private BigDecimal fuelUsedL;
    private BigDecimal avgSpeedKmh;
    private BigDecimal fuelEfficiencyKmL;
    private TripStatus status;
}