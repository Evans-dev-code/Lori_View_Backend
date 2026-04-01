package com.tradingbot.loriview.mapper;

import com.tradingbot.loriview.dto.response.FuelReadingResponse;
import com.tradingbot.loriview.model.FuelReading;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FuelReadingMapper {

    public FuelReadingResponse toResponse(FuelReading reading) {
        if (reading == null) return null;

        return FuelReadingResponse.builder()
                .id(reading.getId())
                .truckId(reading.getTruck() != null
                        ? reading.getTruck().getId() : null)
                .plateNumber(reading.getTruck() != null
                        ? reading.getTruck().getPlateNumber() : null)
                .tripId(reading.getTrip() != null
                        ? reading.getTrip().getId() : null)
                .time(reading.getTime())
                .fuelLevelL(reading.getFuelLevelL())
                .fuelRateLph(reading.getFuelRateLph())
                .build();
    }

    public List<FuelReadingResponse> toResponseList(List<FuelReading> readings) {
        return readings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
