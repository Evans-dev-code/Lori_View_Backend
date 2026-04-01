package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.dto.request.TripRequest;
import com.tradingbot.loriview.dto.response.TripResponse;
import com.tradingbot.loriview.mapper.TripMapper;
import com.tradingbot.loriview.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final TripMapper  tripMapper;

    // POST /api/v1/trips/start — accepts JSON body
    @PostMapping("/start")
    public ResponseEntity<TripResponse> startTrip(
            @Valid @RequestBody TripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                tripMapper.toResponse(
                        tripService.startTrip(
                                request.getTruckId(),
                                request.getDriverId(),
                                request.getOrigin(),
                                request.getDestination()
                        )
                )
        );
    }

    @PatchMapping("/{tripId}/end")
    public ResponseEntity<TripResponse> endTrip(
            @PathVariable Long tripId) {
        return ResponseEntity.ok(
                tripMapper.toResponse(tripService.endTrip(tripId))
        );
    }

    @PatchMapping("/{tripId}/cancel")
    public ResponseEntity<TripResponse> cancelTrip(
            @PathVariable Long tripId) {
        return ResponseEntity.ok(
                tripMapper.toResponse(tripService.cancelTrip(tripId))
        );
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripById(
            @PathVariable Long tripId) {
        return ResponseEntity.ok(
                tripMapper.toResponse(tripService.getTripById(tripId))
        );
    }

    @GetMapping("/truck/{truckId}")
    public ResponseEntity<List<TripResponse>> getTripsByTruck(
            @PathVariable Long truckId) {
        return ResponseEntity.ok(
                tripService.getTripsByTruck(truckId)
                        .stream().map(tripMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/truck/{truckId}/ongoing")
    public ResponseEntity<TripResponse> getOngoingTrip(
            @PathVariable Long truckId) {
        return ResponseEntity.ok(
                tripMapper.toResponse(tripService.getOngoingTrip(truckId))
        );
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<TripResponse>> getAllTripsByOwner(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(
                tripService.getAllTripsByOwner(ownerId)
                        .stream().map(tripMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/truck/{truckId}/range")
    public ResponseEntity<List<TripResponse>> getTripsByDateRange(
            @PathVariable Long truckId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime to) {
        return ResponseEntity.ok(
                tripService.getTripsByDateRange(truckId, from, to)
                        .stream().map(tripMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }
}