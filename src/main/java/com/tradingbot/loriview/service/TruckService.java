package com.tradingbot.loriview.service;

import com.tradingbot.loriview.enums.TruckStatus;
import com.tradingbot.loriview.model.Truck;
import com.tradingbot.loriview.repository.TruckRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TruckService {

    private final TruckRepository truckRepository;

    // Get all trucks for an owner
    public List<Truck> getTrucksByOwner(Long ownerId) {
        return truckRepository.findByOwnerId(ownerId);
    }

    // Get one truck by ID
    public Truck getTruckById(Long truckId) {
        return truckRepository.findById(truckId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Truck not found with id: " + truckId));
    }

    // Get truck by GPS device IMEI
    public Truck getTruckByImei(String imei) {
        return truckRepository.findByDeviceImei(imei)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No truck registered with IMEI: " + imei));
    }

    // Register a new truck
    @Transactional
    public Truck registerTruck(Truck truck) {
        if (truckRepository.findByPlateNumber(truck.getPlateNumber()).isPresent()) {
            throw new IllegalArgumentException(
                    "Truck with plate " + truck.getPlateNumber() + " already exists");
        }
        return truckRepository.save(truck);
    }

    // Update truck details
    @Transactional
    public Truck updateTruck(Long truckId, Truck updated) {
        Truck existing = getTruckById(truckId);
        existing.setMake(updated.getMake());
        existing.setModel(updated.getModel());
        existing.setYear(updated.getYear());
        existing.setFuelCapacityL(updated.getFuelCapacityL());
        existing.setStatus(updated.getStatus());
        return truckRepository.save(existing);
    }

    // Change truck status — converts String from request to enum
    @Transactional
    public Truck updateTruckStatus(Long truckId, String status) {
        Truck truck = getTruckById(truckId);
        truck.setStatus(TruckStatus.valueOf(status.toUpperCase()));
        return truckRepository.save(truck);
    }

    // Get only active trucks for an owner
    public List<Truck> getActiveTrucks(Long ownerId) {
        return truckRepository.findByOwnerIdAndStatus(ownerId, TruckStatus.ACTIVE);
    }

    // Delete a truck
    @Transactional
    public void deleteTruck(Long truckId) {
        Truck truck = getTruckById(truckId);
        truckRepository.delete(truck);
    }
}