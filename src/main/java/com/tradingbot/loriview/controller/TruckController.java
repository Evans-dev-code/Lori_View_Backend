package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.dto.request.TruckRequest;
import com.tradingbot.loriview.dto.response.TruckResponse;
import com.tradingbot.loriview.mapper.TruckMapper;
import com.tradingbot.loriview.model.Owner;
import com.tradingbot.loriview.model.Truck;
import com.tradingbot.loriview.repository.OwnerRepository;
import com.tradingbot.loriview.service.TruckService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trucks")
@RequiredArgsConstructor
public class TruckController {

    private final TruckService truckService;
    private final TruckMapper truckMapper;
    private final OwnerRepository ownerRepository;

    // GET /api/v1/trucks/owner/{ownerId}
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<TruckResponse>> getTrucksByOwner(
            @PathVariable Long ownerId) {
        List<TruckResponse> trucks = truckService.getTrucksByOwner(ownerId)
                .stream()
                .map(truckMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(trucks);
    }

    // GET /api/v1/trucks/{truckId}
    @GetMapping("/{truckId}")
    public ResponseEntity<TruckResponse> getTruckById(
            @PathVariable Long truckId) {
        return ResponseEntity.ok(
                truckMapper.toResponse(truckService.getTruckById(truckId))
        );
    }

    // GET /api/v1/trucks/owner/{ownerId}/active
    @GetMapping("/owner/{ownerId}/active")
    public ResponseEntity<List<TruckResponse>> getActiveTrucks(
            @PathVariable Long ownerId) {
        List<TruckResponse> trucks = truckService.getActiveTrucks(ownerId)
                .stream()
                .map(truckMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(trucks);
    }

    // POST /api/v1/trucks
    @PostMapping
    public ResponseEntity<TruckResponse> registerTruck(
            @Valid @RequestBody TruckRequest request) {
        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Owner not found: " + request.getOwnerId()));
        Truck truck = truckMapper.toEntity(request, owner);
        Truck saved = truckService.registerTruck(truck);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(truckMapper.toResponse(saved));
    }

    // PUT /api/v1/trucks/{truckId}
    @PutMapping("/{truckId}")
    public ResponseEntity<TruckResponse> updateTruck(
            @PathVariable Long truckId,
            @Valid @RequestBody TruckRequest request) {
        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Owner not found: " + request.getOwnerId()));
        Truck updated = truckMapper.toEntity(request, owner);
        return ResponseEntity.ok(
                truckMapper.toResponse(truckService.updateTruck(truckId, updated))
        );
    }

    // PATCH /api/v1/trucks/{truckId}/status
    @PatchMapping("/{truckId}/status")
    public ResponseEntity<TruckResponse> updateStatus(
            @PathVariable Long truckId,
            @RequestParam String status) {
        return ResponseEntity.ok(
                truckMapper.toResponse(
                        truckService.updateTruckStatus(truckId, status))
        );
    }

    // DELETE /api/v1/trucks/{truckId}
    @DeleteMapping("/{truckId}")
    public ResponseEntity<Void> deleteTruck(@PathVariable Long truckId) {
        truckService.deleteTruck(truckId);
        return ResponseEntity.noContent().build();
    }
}