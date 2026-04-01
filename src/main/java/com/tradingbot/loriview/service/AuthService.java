package com.tradingbot.loriview.service;

import com.tradingbot.loriview.dto.request.LoginRequest;
import com.tradingbot.loriview.dto.request.RegisterRequest;
import com.tradingbot.loriview.dto.response.AuthResponse;
import com.tradingbot.loriview.model.Owner;
import com.tradingbot.loriview.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Trial duration in days
    private static final int TRIAL_DAYS = 14;

    // Register a new truck owner
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Check email is not already taken
        if (ownerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "An account with this email already exists");
        }

        // Create owner with trial period
        Owner owner = Owner.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .trialEndsAt(ZonedDateTime.now().plusDays(TRIAL_DAYS))
                .isActive(true)
                .accountStatus("TRIAL")
                .build();

        Owner saved = ownerRepository.save(owner);

        log.info("New owner registered: {} | trial ends: {}",
                saved.getEmail(), saved.getTrialEndsAt());

        // Generate JWT token
        String token = jwtService.generateToken(
                saved.getId(),
                saved.getEmail(),
                saved.getFullName(),
                "OWNER"
        );

        return buildAuthResponse(saved, token);
    }

    // Login existing owner
    public AuthResponse login(LoginRequest request) {

        Owner owner = ownerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(), owner.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check account is not suspended
        if (!owner.getIsActive()) {
            throw new IllegalStateException(
                    "Your account has been suspended. Contact support.");
        }

        // Update account status based on trial/subscription
        updateAccountStatus(owner);

        String token = jwtService.generateToken(
                owner.getId(),
                owner.getEmail(),
                owner.getFullName(),
                "OWNER"
        );

        log.info("Owner logged in: {}", owner.getEmail());

        return buildAuthResponse(owner, token);
    }

    // Check if owner has active access
    // (either valid trial OR active subscription)
    public boolean hasActiveAccess(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElse(null);

        if (owner == null || !owner.getIsActive()) {
            return false;
        }

        // Still in trial period
        if (owner.getTrialEndsAt() != null &&
                ZonedDateTime.now().isBefore(owner.getTrialEndsAt())) {
            return true;
        }

        // Has active subscription
        return "ACTIVE".equals(owner.getAccountStatus());
    }

    // Get days remaining in trial
    public long getTrialDaysRemaining(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElse(null);

        if (owner == null || owner.getTrialEndsAt() == null) {
            return 0;
        }

        long days = ZonedDateTime.now()
                .until(owner.getTrialEndsAt(),
                        java.time.temporal.ChronoUnit.DAYS);

        return Math.max(0, days);
    }

    // Update owner status based on current state
    @Transactional
    public void updateAccountStatus(Owner owner) {
        boolean inTrial = owner.getTrialEndsAt() != null &&
                ZonedDateTime.now().isBefore(owner.getTrialEndsAt());

        if (inTrial && !"ACTIVE".equals(owner.getAccountStatus())) {
            owner.setAccountStatus("TRIAL");
        } else if (!inTrial && "TRIAL".equals(owner.getAccountStatus())) {
            owner.setAccountStatus("EXPIRED");
        }

        ownerRepository.save(owner);
    }

    // Build the auth response object
    private AuthResponse buildAuthResponse(Owner owner, String token) {
        long trialDaysRemaining = 0;
        if (owner.getTrialEndsAt() != null) {
            trialDaysRemaining = ZonedDateTime.now()
                    .until(owner.getTrialEndsAt(),
                            java.time.temporal.ChronoUnit.DAYS);
            trialDaysRemaining = Math.max(0, trialDaysRemaining);
        }

        return AuthResponse.builder()
                .token(token)
                .ownerId(owner.getId())
                .fullName(owner.getFullName())
                .email(owner.getEmail())
                .phone(owner.getPhone())
                .accountStatus(owner.getAccountStatus())
                .trialDaysRemaining(trialDaysRemaining)
                .role("OWNER")
                .build();
    }
}