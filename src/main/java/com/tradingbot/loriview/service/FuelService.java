package com.tradingbot.loriview.service;

import com.tradingbot.loriview.enums.AlertSeverity;
import com.tradingbot.loriview.enums.AlertType;
import com.tradingbot.loriview.model.FuelReading;
import com.tradingbot.loriview.model.Truck;
import com.tradingbot.loriview.repository.FuelReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuelService {

    private final FuelReadingRepository fuelReadingRepository;
    private final TruckService truckService;
    private final AlertService alertService;

    // Threshold — if fuel drops more than this in one reading, raise alert
    private static final BigDecimal FUEL_DROP_THRESHOLD_L = new BigDecimal("20.0");

    // Save a new fuel reading from the tracker
    @Transactional
    public FuelReading saveFuelReading(String deviceImei,
                                       BigDecimal fuelLevelL,
                                       BigDecimal fuelRateLph,
                                       Long tripId) {

        Truck truck = truckService.getTruckByImei(deviceImei);

        // Check for sudden fuel drop (possible theft)
        checkForFuelTheft(truck, fuelLevelL);

        FuelReading reading = FuelReading.builder()
                .time(ZonedDateTime.now())
                .truck(truck)
                .fuelLevelL(fuelLevelL)
                .fuelRateLph(fuelRateLph)
                .build();

        return fuelReadingRepository.save(reading);
    }

    // Detect sudden fuel drop — possible siphoning or theft
    private void checkForFuelTheft(Truck truck, BigDecimal currentLevel) {
        Optional<FuelReading> lastReading =
                fuelReadingRepository.findLatestByTruckId(truck.getId());

        lastReading.ifPresent(last -> {
            BigDecimal drop = last.getFuelLevelL().subtract(currentLevel);
            if (drop.compareTo(FUEL_DROP_THRESHOLD_L) > 0) {
                log.warn("Fuel drop detected on truck {}: {}L dropped",
                        truck.getPlateNumber(), drop);
                alertService.createAlert(
                        truck,
                        null,
                        AlertType.FUEL_DROP,        // was "FUEL_DROP"
                        AlertSeverity.CRITICAL,     // was "critical"
                        String.format("Sudden fuel drop of %.1fL detected on truck %s. " +
                                "Possible fuel theft or leak.", drop, truck.getPlateNumber()),
                        null, null
                );
            }
        });
    }

    // Get current fuel level for a truck
    public Optional<FuelReading> getLatestFuelReading(Long truckId) {
        return fuelReadingRepository.findLatestByTruckId(truckId);
    }

    // Calculate total fuel used on a trip
    public BigDecimal calculateFuelUsedOnTrip(Long tripId) {
        List<FuelReading> readings = fuelReadingRepository.findByTripIdOrderByTimeAsc(tripId);

        if (readings.isEmpty()) return BigDecimal.ZERO;

        // Fuel used = first reading level minus last reading level
        BigDecimal startLevel = readings.get(0).getFuelLevelL();
        BigDecimal endLevel = readings.get(readings.size() - 1).getFuelLevelL();

        BigDecimal used = startLevel.subtract(endLevel);
        return used.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO
                : used.setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate fuel efficiency in km per litre
    public BigDecimal calculateFuelEfficiency(BigDecimal distanceKm,
                                              BigDecimal fuelUsedL) {
        if (fuelUsedL == null || fuelUsedL.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return distanceKm.divide(fuelUsedL, 2, RoundingMode.HALF_UP);
    }

    // Get fuel readings over a time range
    public List<FuelReading> getFuelReadingsInRange(Long truckId,
                                                    ZonedDateTime from,
                                                    ZonedDateTime to) {
        return fuelReadingRepository.findByTruckIdAndTimeRange(truckId, from, to);
    }
}