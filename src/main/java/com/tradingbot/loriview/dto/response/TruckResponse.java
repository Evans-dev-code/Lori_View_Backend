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
public class TruckResponse {

    private Long id;
    private String plateNumber;
    private String make;
    private String model;
    private Integer year;
    private BigDecimal fuelCapacityL;
    private String deviceImei;
    private TruckStatus status;
    private ZonedDateTime createdAt;

    // Live data — populated separately when needed
    private BigDecimal currentFuelLevel;
    private BigDecimal currentSpeedKmh;
    private BigDecimal lastLatitude;
    private BigDecimal lastLongitude;
}