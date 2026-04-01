package com.tradingbot.loriview.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long ownerId;
    private String fullName;
    private String email;
    private String phone;
    private String role;

    // TRIAL | ACTIVE | SUSPENDED | EXPIRED
    private String accountStatus;

    // How many trial days are left (0 if subscribed)
    private long trialDaysRemaining;
}