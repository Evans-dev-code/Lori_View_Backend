package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.dto.request.LoginRequest;
import com.tradingbot.loriview.dto.request.RegisterRequest;
import com.tradingbot.loriview.dto.request.UpdateProfileRequest;
import com.tradingbot.loriview.dto.response.AuthResponse;
import com.tradingbot.loriview.model.Owner;
import com.tradingbot.loriview.repository.OwnerRepository;
import com.tradingbot.loriview.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tradingbot.loriview.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService      jwtService;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;

    // POST /api/v1/auth/register
    // New truck owner signs up
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    // POST /api/v1/auth/login
    // Existing owner logs in
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        Owner owner = ownerRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(
                request.getPassword(), owner.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("Owner logged in: {}", owner.getEmail());

        long trialDays = 0;
        if (owner.getTrialEndsAt() != null) {
            trialDays = Math.max(0,
                    ZonedDateTime.now().until(
                            owner.getTrialEndsAt(),
                            java.time.temporal.ChronoUnit.DAYS
                    )
            );
        }

        // Use role from database — OWNER or SUPER_ADMIN
        String role = owner.getRole() != null
                ? owner.getRole() : "OWNER";

        String token = jwtService.generateToken(
                owner.getId(),
                owner.getEmail(),
                owner.getFullName(),
                role               // ← role from DB, not hardcoded
        );

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .token(token)
                        .ownerId(owner.getId())
                        .fullName(owner.getFullName())
                        .email(owner.getEmail())
                        .phone(owner.getPhone())
                        .role(role)
                        .accountStatus(owner.getAccountStatus())
                        .trialDaysRemaining(trialDays)
                        .build()
        );
    }

    // GET /api/v1/auth/trial-status/{ownerId}
    // Check trial days remaining
    @GetMapping("/trial-status/{ownerId}")
    public ResponseEntity<Long> getTrialStatus(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(
                authService.getTrialDaysRemaining(ownerId));
    }

    // GET /api/v1/auth/profile
    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        String token   = authHeader.replace("Bearer ", "");
        Long   ownerId = jwtService.extractOwnerId(token);

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found"));

        long trialDays = 0;
        if (owner.getTrialEndsAt() != null) {
            trialDays = Math.max(0,
                    ZonedDateTime.now().until(
                            owner.getTrialEndsAt(),
                            java.time.temporal.ChronoUnit.DAYS
                    )
            );
        }

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .ownerId(owner.getId())
                .fullName(owner.getFullName())
                .email(owner.getEmail())
                .phone(owner.getPhone())
                .accountStatus(owner.getAccountStatus())
                .trialDaysRemaining(trialDays)
                .role("OWNER")
                .build();

        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/auth/profile
    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request) {

        String token   = authHeader.replace("Bearer ", "");
        Long   ownerId = jwtService.extractOwnerId(token);

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found"));

        if (request.getFullName() != null) {
            owner.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            owner.setPhone(request.getPhone());
        }
        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            owner.setPasswordHash(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        ownerRepository.save(owner);

        String newToken = jwtService.generateToken(
                owner.getId(), owner.getEmail(),
                owner.getFullName(), "OWNER"
        );

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .token(newToken)
                        .ownerId(owner.getId())
                        .fullName(owner.getFullName())
                        .email(owner.getEmail())
                        .phone(owner.getPhone())
                        .accountStatus(owner.getAccountStatus())
                        .role("OWNER")
                        .build()
        );
    }
}