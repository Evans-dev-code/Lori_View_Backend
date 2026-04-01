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
public class TruckRequest {

    @NotNull
    private Long ownerId;

    @NotBlank
    private String plateNumber;

    private String make;
    private String model;
    private Integer year;
    private BigDecimal fuelCapacityL;
    private String deviceImei;
}