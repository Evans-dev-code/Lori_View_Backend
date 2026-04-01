package com.tradingbot.loriview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {

    @NotNull
    private Long truckId;

    private Long driverId;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;
}