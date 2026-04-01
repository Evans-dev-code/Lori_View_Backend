package com.tradingbot.loriview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelReadingRequest {

    @NotBlank
    private String deviceImei;

    @NotNull
    private BigDecimal fuelLevelL;

    private BigDecimal fuelRateLph;
    private Long tripId;
}