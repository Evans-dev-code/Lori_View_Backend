package com.tradingbot.loriview.service;

import com.tradingbot.loriview.dto.request.TruckRequest;
import com.tradingbot.loriview.dto.response.AdminDashboardResponse;
import com.tradingbot.loriview.model.Owner;
import com.tradingbot.loriview.model.Payment;
import com.tradingbot.loriview.model.Subscription;
import com.tradingbot.loriview.model.Truck;
import com.tradingbot.loriview.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OwnerRepository        ownerRepository;
    private final TruckRepository        truckRepository;
    private final TripRepository         tripRepository;
    private final PaymentRepository      paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TruckService           truckService;

    public AdminDashboardResponse getDashboard() {

        ZonedDateTime monthStart = ZonedDateTime.now()
                .withDayOfMonth(1).withHour(0)
                .withMinute(0).withSecond(0);

        BigDecimal totalRevenue   = paymentRepository
                .sumSuccessfulPayments().orElse(BigDecimal.ZERO);
        BigDecimal monthlyRevenue = paymentRepository
                .sumPaymentsSince(monthStart).orElse(BigDecimal.ZERO);

        return AdminDashboardResponse.builder()
                .totalOwners(ownerRepository.count())
                .totalTrucks(truckRepository.count())
                .totalTrips(tripRepository.count())
                .activeSubscribers(subscriptionRepository.countByIsActiveTrue())
                .trialOwners(ownerRepository.countByAccountStatus("TRIAL"))
                .activeOwners(ownerRepository.countByAccountStatus("ACTIVE"))
                .expiredOwners(ownerRepository.countByAccountStatus("EXPIRED"))
                .totalRevenueKes(totalRevenue)
                .monthlyRevenueKes(monthlyRevenue)
                .newOwnersThisMonth(
                        ownerRepository.countByCreatedAtAfter(monthStart))
                .tripsThisMonth(
                        tripRepository.countByStartedAtAfter(monthStart))
                .build();
    }

    public List<Owner> getAllOwners() {
        return ownerRepository.findAllByOrderByCreatedAtDesc();
    }

    public Owner getOwnerById(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Owner not found: " + id));
    }

    public List<Truck> getTrucksForOwner(Long ownerId) {
        return truckRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public List<Payment> getPaymentsForOwner(Long ownerId) {
        return paymentRepository
                .findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public List<Subscription> getSubscriptionsForOwner(Long ownerId) {
        return subscriptionRepository
                .findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAllByOrderByCreatedAtDesc();
    }

    // Admin adds a truck for a specific owner
    @Transactional
    public Truck addTruckForOwner(Long ownerId, TruckRequest request) {

        // 1. Fetch the actual Owner entity from the database using the ID
        Owner truckOwner = getOwnerById(ownerId);

        // 2. Translate the TruckRequest into a real Truck entity
        Truck truck = new Truck();

        // FIX: Pass the actual Owner object, not the Long ID!
        truck.setOwner(truckOwner);

        truck.setPlateNumber(request.getPlateNumber());
        truck.setMake(request.getMake());
        truck.setModel(request.getModel());
        truck.setYear(request.getYear());
        truck.setFuelCapacityL(request.getFuelCapacityL());
        truck.setDeviceImei(request.getDeviceImei());
        truck.setStatus(com.tradingbot.loriview.enums.TruckStatus.ACTIVE);

        // 3. Pass the translated Truck entity to your TruckService
        return truckService.registerTruck(truck);
    }
    // Suspend owner account
    @Transactional
    public Owner suspendOwner(Long ownerId) {
        Owner owner = getOwnerById(ownerId);
        owner.setAccountStatus("SUSPENDED");
        owner.setIsActive(false);
        return ownerRepository.save(owner);
    }

    // Reactivate owner account
    @Transactional
    public Owner activateOwner(Long ownerId) {
        Owner owner = getOwnerById(ownerId);
        owner.setAccountStatus("ACTIVE");
        owner.setIsActive(true);
        return ownerRepository.save(owner);
    }
}