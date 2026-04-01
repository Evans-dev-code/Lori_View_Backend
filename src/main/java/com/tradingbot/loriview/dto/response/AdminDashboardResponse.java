package com.tradingbot.loriview.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminDashboardResponse {
    private long       totalOwners;
    private long       totalTrucks;
    private long       totalTrips;
    private long       activeSubscribers;
    private long       trialOwners;
    private long       activeOwners;
    private long       expiredOwners;
    private BigDecimal totalRevenueKes;
    private BigDecimal monthlyRevenueKes;
    private long       newOwnersThisMonth;
    private long       tripsThisMonth;
}