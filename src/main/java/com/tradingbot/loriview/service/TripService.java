package com.tradingbot.loriview.service;

import com.tradingbot.loriview.enums.TripStatus;
import com.tradingbot.loriview.model.Trip;
import com.tradingbot.loriview.model.Truck;
import com.tradingbot.loriview.repository.TripRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TruckService truckService;

    // Start a new trip
    @Transactional
    public Trip startTrip(Long truckId,
                          Long driverId,
                          String origin,
                          String destination) {

        // Check truck does not already have an ongoing trip
        tripRepository.findByTruckIdAndStatus(truckId, TripStatus.ONGOING)
                .ifPresent(t -> {
                    throw new IllegalStateException(
                            "Truck already has an ongoing trip to: "
                                    + t.getDestination());
                });

        Truck truck = truckService.getTruckById(truckId);

        Trip trip = Trip.builder()
                .truck(truck)
                .origin(origin)
                .destination(destination)
                .startedAt(ZonedDateTime.now())
                .status(TripStatus.ONGOING)
                .build();

        return tripRepository.save(trip);
    }

    // End a trip
    @Transactional
    public Trip endTrip(Long tripId) {
        Trip trip = getTripById(tripId);

        if (!trip.getStatus().equals(TripStatus.ONGOING)) {
            throw new IllegalStateException(
                    "Trip is not ongoing, cannot end it");
        }

        trip.setEndedAt(ZonedDateTime.now());
        trip.setStatus(TripStatus.COMPLETED);
        return tripRepository.save(trip);
    }

    // Get a single trip
    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trip not found with id: " + tripId));
    }

    // Get all trips for a truck
    public List<Trip> getTripsByTruck(Long truckId) {
        return tripRepository.findByTruckIdOrderByStartedAtDesc(truckId);
    }

    // Get current ongoing trip for a truck
    public Trip getOngoingTrip(Long truckId) {
        return tripRepository.findByTruckIdAndStatus(truckId, TripStatus.ONGOING)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No ongoing trip for truck: " + truckId));
    }

    // Get all trips for an owner's fleet
    public List<Trip> getAllTripsByOwner(Long ownerId) {
        return tripRepository.findAllByOwnerId(ownerId);
    }

    // Get trips within a date range
    public List<Trip> getTripsByDateRange(Long truckId,
                                          ZonedDateTime from,
                                          ZonedDateTime to) {
        return tripRepository.findByTruckIdAndDateRange(truckId, from, to);
    }

    // Cancel a trip
    @Transactional
    public Trip cancelTrip(Long tripId) {
        Trip trip = getTripById(tripId);
        trip.setStatus(TripStatus.CANCELLED);
        trip.setEndedAt(ZonedDateTime.now());
        return tripRepository.save(trip);
    }
}