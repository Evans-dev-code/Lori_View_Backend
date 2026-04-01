package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.dto.request.LocationPingRequest;
import com.tradingbot.loriview.dto.response.LocationPingResponse;
import com.tradingbot.loriview.mapper.LocationPingMapper;
import com.tradingbot.loriview.model.LocationPing;
import com.tradingbot.loriview.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationPingMapper locationPingMapper;

    // POST /api/v1/location/ping
    // GPS tracker device calls this every 10-30 seconds
    @PostMapping("/ping")
    public ResponseEntity<LocationPingResponse> receivePing(
            @RequestBody Map<String, Object> body) {

        String deviceImei   = (String)  body.get("deviceImei");
        Double latitude     = (Double)  body.get("latitude");
        Double longitude    = (Double)  body.get("longitude");
        Double speedKmh     = (Double)  body.getOrDefault("speedKmh", 0.0);
        Double headingDeg   = (Double)  body.getOrDefault("headingDeg", 0.0);
        Double altitudeM    = (Double)  body.getOrDefault("altitudeM", 0.0);
        Integer satellites  = (Integer) body.getOrDefault("satellites", 0);

        LocationPing ping = locationService.saveLocationPing(
                deviceImei,
                new java.math.BigDecimal(latitude.toString()),
                new java.math.BigDecimal(longitude.toString()),
                new java.math.BigDecimal(speedKmh.toString()),
                new java.math.BigDecimal(headingDeg.toString()),
                new java.math.BigDecimal(altitudeM.toString()),
                satellites
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationPingMapper.toResponse(ping));
    }

    // GET /api/v1/location/truck/{truckId}/live
    @GetMapping("/truck/{truckId}/live")
    public ResponseEntity<LocationPingResponse> getLiveLocation(
            @PathVariable Long truckId) {
        return locationService.getLatestLocation(truckId)
                .map(ping -> ResponseEntity.ok(locationPingMapper.toResponse(ping)))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/location/trip/{tripId}/route
    // Returns ordered list of pings — Angular draws this as a route on the map
    @GetMapping("/trip/{tripId}/route")
    public ResponseEntity<List<LocationPingResponse>> getTripRoute(
            @PathVariable Long tripId) {
        return ResponseEntity.ok(
                locationPingMapper.toResponseList(
                        locationService.getTripRoute(tripId))
        );
    }

    // GET /api/v1/location/truck/{truckId}/range
    @GetMapping("/truck/{truckId}/range")
    public ResponseEntity<List<LocationPingResponse>> getPingsInRange(
            @PathVariable Long truckId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime to) {
        return ResponseEntity.ok(
                locationPingMapper.toResponseList(
                        locationService.getPingsInRange(truckId, from, to))
        );
    }
}