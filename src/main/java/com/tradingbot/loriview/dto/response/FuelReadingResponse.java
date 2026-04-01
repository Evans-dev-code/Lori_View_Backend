package com.tradingbot.loriview.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelReadingResponse {

    private Long id;
    private Long truckId;
    private String plateNumber;
    private Long tripId;
    private ZonedDateTime time;
    private BigDecimal fuelLevelL;
    private BigDecimal fuelRateLph;
}