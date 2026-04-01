package com.tradingbot.loriview.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationPingResponse {

    private Long truckId;
    private String plateNumber;
    private Long tripId;
    private ZonedDateTime time;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speedKmh;
    private BigDecimal headingDeg;
    private BigDecimal altitudeM;
    private Integer satellites;
}