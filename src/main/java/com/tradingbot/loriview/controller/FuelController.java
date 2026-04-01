package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.dto.request.FuelReadingRequest;
import com.tradingbot.loriview.dto.response.FuelReadingResponse;
import com.tradingbot.loriview.mapper.FuelReadingMapper;
import com.tradingbot.loriview.service.FuelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fuel")
@RequiredArgsConstructor
public class FuelController {

    private final FuelService fuelService;
    private final FuelReadingMapper fuelReadingMapper;

    // POST /api/v1/fuel/reading
    // Fuel sensor data arrives here
    @PostMapping("/reading")
    public ResponseEntity<FuelReadingResponse> receiveFuelReading(
            @Valid @RequestBody FuelReadingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                fuelReadingMapper.toResponse(
                        fuelService.saveFuelReading(
                                request.getDeviceImei(),
                                request.getFuelLevelL(),
                                request.getFuelRateLph(),
                                request.getTripId()
                        )
                )
        );
    }

    // GET /api/v1/fuel/truck/{truckId}/current
    @GetMapping("/truck/{truckId}/current")
    public ResponseEntity<FuelReadingResponse> getCurrentFuelLevel(
            @PathVariable Long truckId) {
        return fuelService.getLatestFuelReading(truckId)
                .map(reading -> ResponseEntity.ok(
                        fuelReadingMapper.toResponse(reading)))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/fuel/trip/{tripId}/used
    @GetMapping("/trip/{tripId}/used")
    public ResponseEntity<BigDecimal> getFuelUsedOnTrip(
            @PathVariable Long tripId) {
        return ResponseEntity.ok(fuelService.calculateFuelUsedOnTrip(tripId));
    }

    // GET /api/v1/fuel/truck/{truckId}/range
    @GetMapping("/truck/{truckId}/range")
    public ResponseEntity<List<FuelReadingResponse>> getFuelReadingsInRange(
            @PathVariable Long truckId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime to) {
        return ResponseEntity.ok(
                fuelReadingMapper.toResponseList(
                        fuelService.getFuelReadingsInRange(truckId, from, to))
        );
    }

    // GET /api/v1/fuel/efficiency
    @GetMapping("/efficiency")
    public ResponseEntity<BigDecimal> getFuelEfficiency(
            @RequestParam BigDecimal distanceKm,
            @RequestParam BigDecimal fuelUsedL) {
        return ResponseEntity.ok(
                fuelService.calculateFuelEfficiency(distanceKm, fuelUsedL)
        );
    }
}