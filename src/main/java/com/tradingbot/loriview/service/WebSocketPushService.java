package com.tradingbot.loriview.service;

import com.tradingbot.loriview.dto.response.LocationPingResponse;
import com.tradingbot.loriview.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final SimpMessagingTemplate messagingTemplate;

    // Push live location to Angular
    // Angular subscribes to /topic/location/{ownerId}
    public void pushLocation(Long ownerId,
                             LocationPingResponse ping) {
        String destination = "/topic/location/" + ownerId;
        messagingTemplate.convertAndSend(destination, ping);
        log.debug("Pushed location to {}", destination);
    }

    // Push new alert to Angular
    // Angular subscribes to /topic/alerts/{ownerId}
    public void pushAlert(Long ownerId, Alert alert) {
        String destination = "/topic/alerts/" + ownerId;
        messagingTemplate.convertAndSend(destination, alert);
        log.debug("Pushed alert to {}", destination);
    }

    // Push fleet summary update
    public void pushFleetUpdate(Long ownerId, Object data) {
        String destination = "/topic/fleet/" + ownerId;
        messagingTemplate.convertAndSend(destination, data);
    }
}