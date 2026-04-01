package com.tradingbot.loriview.service;

import com.tradingbot.loriview.dto.response.LocationPingResponse;
import com.tradingbot.loriview.enums.AlertSeverity;
import com.tradingbot.loriview.enums.AlertType;
import com.tradingbot.loriview.mapper.LocationPingMapper;
import com.tradingbot.loriview.model.LocationPing;
import com.tradingbot.loriview.model.Truck;
import com.tradingbot.loriview.repository.LocationPingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationPingRepository  locationPingRepository;
    private final TruckService            truckService;
    private final AlertService            alertService;
    private final WebSocketPushService    wsPushService;
    private final LocationPingMapper      locationPingMapper;

    private static final BigDecimal SPEED_LIMIT = new BigDecimal("90.0");

    @Transactional
    public LocationPing saveLocationPing(String deviceImei,
                                         BigDecimal latitude,
                                         BigDecimal longitude,
                                         BigDecimal speedKmh,
                                         BigDecimal headingDeg,
                                         BigDecimal altitudeM,
                                         Integer satellites) {

        Truck truck = truckService.getTruckByImei(deviceImei);

        checkForSpeeding(truck, speedKmh, latitude, longitude);

        LocationPing ping = LocationPing.builder()
                .time(ZonedDateTime.now())
                .truck(truck)
                .latitude(latitude)
                .longitude(longitude)
                .speedKmh(speedKmh)
                .headingDeg(headingDeg)
                .altitudeM(altitudeM)
                .satellites(satellites)
                .build();

        LocationPing saved = locationPingRepository.save(ping);

        // Push to Angular via WebSocket
        try {
            LocationPingResponse response = locationPingMapper.toResponse(saved);
            wsPushService.pushLocation(truck.getOwner().getId(), response);
        } catch (Exception e) {
            log.warn("WebSocket push failed: {}", e.getMessage());
        }

        return saved;
    }

    private void checkForSpeeding(Truck truck, BigDecimal speed,
                                  BigDecimal lat, BigDecimal lng) {
        if (speed != null && speed.compareTo(SPEED_LIMIT) > 0) {
            log.warn("Speeding: truck {} at {} km/h",
                    truck.getPlateNumber(), speed);
            alertService.createAlert(
                    truck, null,
                    AlertType.SPEEDING,
                    AlertSeverity.HIGH,
                    String.format("Truck %s doing %.1f km/h (limit: %s km/h)",
                            truck.getPlateNumber(), speed, SPEED_LIMIT),
                    lat, lng
            );
        }
    }

    public Optional<LocationPing> getLatestLocation(Long truckId) {
        return locationPingRepository.findLatestByTruckId(truckId);
    }

    public List<LocationPing> getTripRoute(Long tripId) {
        return locationPingRepository.findByTripIdOrderByTimeAsc(tripId);
    }

    public List<LocationPing> getPingsInRange(Long truckId,
                                              ZonedDateTime from,
                                              ZonedDateTime to) {
        return locationPingRepository.findByTruckIdAndTimeRange(
                truckId, from, to
        );
    }
}