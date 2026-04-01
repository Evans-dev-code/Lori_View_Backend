package com.tradingbot.loriview.dto.response;

import com.tradingbot.loriview.enums.AlertSeverity;
import com.tradingbot.loriview.enums.AlertType;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {

    private Long id;
    private Long truckId;
    private String plateNumber;
    private AlertType type;
    private AlertSeverity severity;
    private String message;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isRead;
    private ZonedDateTime triggeredAt;
}