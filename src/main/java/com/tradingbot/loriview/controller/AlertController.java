package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.dto.response.AlertResponse;
import com.tradingbot.loriview.mapper.AlertMapper;
import com.tradingbot.loriview.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AlertMapper alertMapper;

    // GET /api/v1/alerts/owner/{ownerId}
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<AlertResponse>> getAllAlertsByOwner(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(
                alertMapper.toResponseList(
                        alertService.getAllAlertsForOwner(ownerId))
        );
    }

    // GET /api/v1/alerts/truck/{truckId}/unread
    @GetMapping("/truck/{truckId}/unread")
    public ResponseEntity<List<AlertResponse>> getUnreadAlerts(
            @PathVariable Long truckId) {
        return ResponseEntity.ok(
                alertMapper.toResponseList(
                        alertService.getUnreadAlertsForTruck(truckId))
        );
    }

    // GET /api/v1/alerts/owner/{ownerId}/unread-count
    @GetMapping("/owner/{ownerId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long ownerId) {
        return ResponseEntity.ok(alertService.countUnreadAlerts(ownerId));
    }

    // PATCH /api/v1/alerts/truck/{truckId}/mark-read
    @PatchMapping("/truck/{truckId}/mark-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long truckId) {
        alertService.markAllAsRead(truckId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/alerts/truck/{truckId}/type/{type}
    @GetMapping("/truck/{truckId}/type/{type}")
    public ResponseEntity<List<AlertResponse>> getAlertsByType(
            @PathVariable Long truckId,
            @PathVariable String type) {
        return ResponseEntity.ok(
                alertMapper.toResponseList(
                        alertService.getAlertsByType(truckId, type))
        );
    }
}