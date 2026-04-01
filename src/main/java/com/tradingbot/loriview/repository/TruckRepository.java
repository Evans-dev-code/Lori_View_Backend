package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.enums.TruckStatus;
import com.tradingbot.loriview.model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {

    List<Truck> findByOwnerId(Long ownerId);
    // FIX: Replaced findByOwnerId with this method so AdminService can sort by date
    List<Truck> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Truck> findByDeviceImei(String deviceImei);

    Optional<Truck> findByPlateNumber(String plateNumber);

    // Status is now TruckStatus enum not String
    List<Truck> findByOwnerIdAndStatus(Long ownerId, TruckStatus status);

    long countByOwnerId(Long ownerId);
}