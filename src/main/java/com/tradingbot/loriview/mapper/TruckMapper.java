package com.tradingbot.loriview.mapper;

import com.tradingbot.loriview.dto.request.TruckRequest;
import com.tradingbot.loriview.dto.response.LiveTruckResponse;
import com.tradingbot.loriview.dto.response.TruckResponse;
import com.tradingbot.loriview.model.Owner;
import com.tradingbot.loriview.model.Truck;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TruckMapper {

    // Convert Truck entity → TruckResponse DTO
    public TruckResponse toResponse(Truck truck) {
        if (truck == null) return null;

        return TruckResponse.builder()
                .id(truck.getId())
                .plateNumber(truck.getPlateNumber())
                .make(truck.getMake())
                .model(truck.getModel())
                .year(truck.getYear())
                .fuelCapacityL(truck.getFuelCapacityL())
                .deviceImei(truck.getDeviceImei())
                .status(truck.getStatus())
                .createdAt(truck.getCreatedAt())
                .build();
    }

    // Convert TruckRequest DTO → Truck entity
    public Truck toEntity(TruckRequest request, Owner owner) {
        if (request == null) return null;

        return Truck.builder()
                .owner(owner)
                .plateNumber(request.getPlateNumber())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .fuelCapacityL(request.getFuelCapacityL())
                .deviceImei(request.getDeviceImei())
                .build();
    }

    // Convert Truck entity → LiveTruckResponse DTO
    // Used for the live dashboard map — includes location and fuel
    public LiveTruckResponse toLiveResponse(
            Truck truck,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal speedKmh,
            BigDecimal headingDeg,
            java.time.ZonedDateTime lastSeen,
            BigDecimal fuelLevelL,
            Long currentTripId,
            String currentDestination) {

        if (truck == null) return null;

        // Calculate fuel percentage
        BigDecimal fuelPercentage = BigDecimal.ZERO;
        if (truck.getFuelCapacityL() != null
                && fuelLevelL != null
                && truck.getFuelCapacityL().compareTo(BigDecimal.ZERO) > 0) {
            fuelPercentage = fuelLevelL
                    .divide(truck.getFuelCapacityL(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        return LiveTruckResponse.builder()
                .truckId(truck.getId())
                .plateNumber(truck.getPlateNumber())
                .make(truck.getMake())
                .model(truck.getModel())
                .status(truck.getStatus())
                .latitude(latitude)
                .longitude(longitude)
                .speedKmh(speedKmh)
                .headingDeg(headingDeg)
                .lastSeen(lastSeen)
                .fuelLevelL(fuelLevelL)
                .fuelCapacityL(truck.getFuelCapacityL())
                .fuelPercentage(fuelPercentage)
                .currentTripId(currentTripId)
                .currentDestination(currentDestination)
                .build();
    }
}