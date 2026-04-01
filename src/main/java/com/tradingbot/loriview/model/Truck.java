package com.tradingbot.loriview.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradingbot.loriview.enums.TruckStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "trucks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @NotBlank
    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Column(length = 60)
    private String make;           // e.g. Isuzu, Volvo, MAN

    @Column(length = 60)
    private String model;

    @Column
    private Integer year;

    @Column(name = "fuel_capacity_l", precision = 8, scale = 2)
    private BigDecimal fuelCapacityL;  // tank size in litres

    @Column(name = "device_imei", unique = true, length = 30)
    private String deviceImei;         // GPS tracker IMEI

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TruckStatus status = TruckStatus.ACTIVE;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Trip> trips;
}