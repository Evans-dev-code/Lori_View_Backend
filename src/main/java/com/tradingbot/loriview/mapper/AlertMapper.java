package com.tradingbot.loriview.mapper;

import com.tradingbot.loriview.dto.response.AlertResponse;
import com.tradingbot.loriview.model.Alert;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlertMapper {

    // Convert Alert entity → AlertResponse DTO
    public AlertResponse toResponse(Alert alert) {
        if (alert == null) return null;

        return AlertResponse.builder()
                .id(alert.getId())
                .truckId(alert.getTruck() != null ? alert.getTruck().getId() : null)
                .plateNumber(alert.getTruck() != null ? alert.getTruck().getPlateNumber() : null)
                .type(alert.getType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .isRead(alert.getIsRead())
                .triggeredAt(alert.getTriggeredAt())
                .build();
    }

    // Convert a list of Alert entities → list of AlertResponse DTOs
    public List<AlertResponse> toResponseList(List<Alert> alerts) {
        return alerts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}