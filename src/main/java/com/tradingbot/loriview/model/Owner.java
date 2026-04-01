package com.tradingbot.loriview.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "owners")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 30)
    private String phone;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "trial_ends_at")
    private ZonedDateTime trialEndsAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "account_status", length = 20)
    @Builder.Default
    private String accountStatus = "TRIAL";

    @Column(length = 20)
    @Builder.Default
    private String role = "OWNER";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Truck> trucks;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Driver> drivers;
}