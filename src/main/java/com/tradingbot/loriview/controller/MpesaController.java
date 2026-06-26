package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.model.Payment;
import com.tradingbot.loriview.service.MpesaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/mpesa")
@RequiredArgsConstructor
public class MpesaController {

    private final MpesaService mpesaService;

    // POST /api/v1/mpesa/stk-push
    // Angular calls this when owner clicks "Pay Now"
    @PostMapping("/stk-push")
    public ResponseEntity<?> initiateStkPush(
            @RequestParam Long ownerId,
            @RequestParam Long subscriptionId,
            @RequestParam String phoneNumber,
            @RequestParam BigDecimal amountKes) {

        try {
            log.info("Initiating STK Push for owner: {} with amount: {}", ownerId, amountKes);
            Payment payment = mpesaService.initiateStkPush(
                    ownerId, subscriptionId, phoneNumber, amountKes
            );
            return ResponseEntity.ok(payment);

        } catch (Exception e) {
            // This logs the full stack trace to your Koyeb logs
            log.error("CRITICAL STK ERROR: Failed to initiate payment for owner {}: ", ownerId, e);

            // Return a clear message so you can see it in the Network tab
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to initiate M-Pesa payment");
            errorResponse.put("details", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // POST /api/v1/mpesa/callback
    // Safaricom calls this URL automatically after payment
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(
            @RequestBody Map<String, Object> callbackData) {
        log.info("M-Pesa callback received: {}", callbackData);

        try {
            mpesaService.handleCallback(callbackData);

            Map<String, Object> response = new HashMap<>();
            response.put("ResultCode", 0);
            response.put("ResultDesc", "Accepted");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing M-Pesa callback: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("ResultCode", 1);
            response.put("ResultDesc", "Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}