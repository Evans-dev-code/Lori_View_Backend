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
public class LocationPingRequest {

    @NotBlank
    private String deviceImei;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    private BigDecimal speedKmh;
    private BigDecimal headingDeg;
    private BigDecimal altitudeM;
    private Integer satellites;
}