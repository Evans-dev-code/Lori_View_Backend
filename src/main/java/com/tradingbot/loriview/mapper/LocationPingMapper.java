package com.tradingbot.loriview.mapper;

import com.tradingbot.loriview.dto.request.LocationPingRequest;
import com.tradingbot.loriview.dto.response.LocationPingResponse;
import com.tradingbot.loriview.model.LocationPing;
import com.tradingbot.loriview.model.Trip;
import com.tradingbot.loriview.model.Truck;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationPingMapper {

    // Convert LocationPing entity → LocationPingResponse DTO
    public LocationPingResponse toResponse(LocationPing ping) {
        if (ping == null) return null;

        return LocationPingResponse.builder()
                .truckId(ping.getTruck() != null ? ping.getTruck().getId() : null)
                .plateNumber(ping.getTruck() != null ? ping.getTruck().getPlateNumber() : null)
                .tripId(ping.getTrip() != null ? ping.getTrip().getId() : null)
                .time(ping.getTime())
                .latitude(ping.getLatitude())
                .longitude(ping.getLongitude())
                .speedKmh(ping.getSpeedKmh())
                .headingDeg(ping.getHeadingDeg())
                .altitudeM(ping.getAltitudeM())
                .satellites(ping.getSatellites())
                .build();
    }

    // Convert LocationPingRequest DTO → LocationPing entity
    public LocationPing toEntity(LocationPingRequest request, Truck truck, Trip trip) {
        if (request == null) return null;

        return LocationPing.builder()
                .time(ZonedDateTime.now())
                .truck(truck)
                .trip(trip)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speedKmh(request.getSpeedKmh())
                .headingDeg(request.getHeadingDeg())
                .altitudeM(request.getAltitudeM())
                .satellites(request.getSatellites())
                .build();
    }

    // Convert a list of pings → response list (used for route drawing on map)
    public List<LocationPingResponse> toResponseList(List<LocationPing> pings) {
        return pings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}