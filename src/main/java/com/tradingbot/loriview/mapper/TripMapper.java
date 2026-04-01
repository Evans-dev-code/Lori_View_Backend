package com.tradingbot.loriview.mapper;

import com.tradingbot.loriview.dto.request.TripRequest;
import com.tradingbot.loriview.dto.response.TripResponse;
import com.tradingbot.loriview.model.Driver;
import com.tradingbot.loriview.model.Trip;
import com.tradingbot.loriview.model.Truck;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TripMapper {

    // Convert Trip entity → TripResponse DTO
    public TripResponse toResponse(Trip trip) {
        if (trip == null) return null;

        // Calculate fuel efficiency if we have both values
        BigDecimal fuelEfficiency = null;
        if (trip.getDistanceKm() != null
                && trip.getFuelUsedL() != null
                && trip.getFuelUsedL().compareTo(BigDecimal.ZERO) > 0) {
            fuelEfficiency = trip.getDistanceKm()
                    .divide(trip.getFuelUsedL(), 2, RoundingMode.HALF_UP);
        }

        return TripResponse.builder()
                .id(trip.getId())
                .truckId(trip.getTruck() != null ? trip.getTruck().getId() : null)
                .plateNumber(trip.getTruck() != null ? trip.getTruck().getPlateNumber() : null)
                .driverId(trip.getDriver() != null ? trip.getDriver().getId() : null)
                .driverName(trip.getDriver() != null ? trip.getDriver().getFullName() : null)
                .origin(trip.getOrigin())
                .destination(trip.getDestination())
                .startedAt(trip.getStartedAt())
                .endedAt(trip.getEndedAt())
                .distanceKm(trip.getDistanceKm())
                .fuelUsedL(trip.getFuelUsedL())
                .avgSpeedKmh(trip.getAvgSpeedKmh())
                .fuelEfficiencyKmL(fuelEfficiency)
                .status(trip.getStatus())
                .build();
    }

    // Convert TripRequest DTO → Trip entity
    public Trip toEntity(TripRequest request, Truck truck, Driver driver) {
        if (request == null) return null;

        return Trip.builder()
                .truck(truck)
                .driver(driver)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .build();
    }
}