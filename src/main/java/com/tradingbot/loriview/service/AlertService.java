package com.tradingbot.loriview.service;

import com.tradingbot.loriview.enums.AlertSeverity;
import com.tradingbot.loriview.enums.AlertType;
import com.tradingbot.loriview.model.Alert;
import com.tradingbot.loriview.model.Trip;
import com.tradingbot.loriview.model.Truck;
import com.tradingbot.loriview.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    // Central method — all other services call this to raise an alert
    @Transactional
    public Alert createAlert(Truck truck,
                             Trip trip,
                             AlertType type,
                             AlertSeverity severity,
                             String message,
                             BigDecimal latitude,
                             BigDecimal longitude) {

        log.info("Alert raised | truck: {} | type: {} | severity: {}",
                truck.getPlateNumber(), type, severity);

        Alert alert = Alert.builder()
                .truck(truck)
                .trip(trip)
                .type(type)
                .severity(severity)
                .message(message)
                .latitude(latitude)
                .longitude(longitude)
                .isRead(false)
                .build();

        return alertRepository.save(alert);
    }

    // Get all unread alerts for a truck
    public List<Alert> getUnreadAlertsForTruck(Long truckId) {
        return alertRepository
                .findByTruckIdAndIsReadFalseOrderByTriggeredAtDesc(truckId);
    }

    // Get all alerts for an owner's fleet
    public List<Alert> getAllAlertsForOwner(Long ownerId) {
        return alertRepository.findAllByOwnerId(ownerId);
    }

    // Count unread alerts (used for dashboard notification badge)
    public long countUnreadAlerts(Long ownerId) {
        return alertRepository.countUnreadByOwnerId(ownerId);
    }

    // Mark all alerts as read for a truck
    @Transactional
    public void markAllAsRead(Long truckId) {
        alertRepository.markAllAsReadForTruck(truckId);
    }

    // Get alerts by type
    public List<Alert> getAlertsByType(Long truckId, String type) {
        AlertType alertType = AlertType.valueOf(type.toUpperCase());
        return alertRepository
                .findByTruckIdAndTypeOrderByTriggeredAtDesc(truckId, alertType);
    }
}