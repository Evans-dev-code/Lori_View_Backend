package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.dto.request.TruckRequest;
import com.tradingbot.loriview.dto.response.AdminDashboardResponse;
import com.tradingbot.loriview.dto.response.TruckResponse;
import com.tradingbot.loriview.mapper.TruckMapper;
import com.tradingbot.loriview.model.Owner;
import com.tradingbot.loriview.model.Payment;
import com.tradingbot.loriview.model.Subscription;
import com.tradingbot.loriview.model.Truck;
import com.tradingbot.loriview.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TruckMapper  truckMapper;

    // GET /api/v1/admin/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // GET /api/v1/admin/owners
    @GetMapping("/owners")
    public ResponseEntity<List<Owner>> getAllOwners() {
        return ResponseEntity.ok(adminService.getAllOwners());
    }

    // GET /api/v1/admin/owners/{ownerId}
    @GetMapping("/owners/{ownerId}")
    public ResponseEntity<Owner> getOwner(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(adminService.getOwnerById(ownerId));
    }

    // GET /api/v1/admin/owners/{ownerId}/trucks
    @GetMapping("/owners/{ownerId}/trucks")
    public ResponseEntity<List<TruckResponse>> getOwnerTrucks(
            @PathVariable Long ownerId) {
        List<TruckResponse> trucks = adminService
                .getTrucksForOwner(ownerId)
                .stream().map(truckMapper::toResponse).toList();
        return ResponseEntity.ok(trucks);
    }

    // POST /api/v1/admin/owners/{ownerId}/trucks
    @PostMapping("/owners/{ownerId}/trucks")
    public ResponseEntity<TruckResponse> addTruckForOwner(
            @PathVariable Long ownerId,
            @Valid @RequestBody TruckRequest request) {
        Truck truck = adminService.addTruckForOwner(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(truckMapper.toResponse(truck));
    }

    // GET /api/v1/admin/owners/{ownerId}/payments
    @GetMapping("/owners/{ownerId}/payments")
    public ResponseEntity<List<Payment>> getOwnerPayments(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(
                adminService.getPaymentsForOwner(ownerId)
        );
    }

    // GET /api/v1/admin/owners/{ownerId}/subscriptions
    @GetMapping("/owners/{ownerId}/subscriptions")
    public ResponseEntity<List<Subscription>> getOwnerSubscriptions(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(
                adminService.getSubscriptionsForOwner(ownerId)
        );
    }

    // PATCH /api/v1/admin/owners/{ownerId}/suspend
    @PatchMapping("/owners/{ownerId}/suspend")
    public ResponseEntity<Owner> suspendOwner(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(adminService.suspendOwner(ownerId));
    }

    // PATCH /api/v1/admin/owners/{ownerId}/activate
    @PatchMapping("/owners/{ownerId}/activate")
    public ResponseEntity<Owner> activateOwner(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(adminService.activateOwner(ownerId));
    }

    // GET /api/v1/admin/payments
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    // GET /api/v1/admin/subscriptions
    @GetMapping("/subscriptions")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        return ResponseEntity.ok(adminService.getAllSubscriptions());
    }

    // GET /api/v1/admin/revenue
    @GetMapping("/revenue")
    public ResponseEntity<AdminDashboardResponse> getRevenue() {
        return ResponseEntity.ok(adminService.getDashboard());
    }
}