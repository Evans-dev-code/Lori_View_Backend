package com.tradingbot.loriview.controller;

import com.tradingbot.loriview.model.Payment;
import com.tradingbot.loriview.service.MpesaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public ResponseEntity<Payment> initiateStkPush(
            @RequestParam Long ownerId,
            @RequestParam Long subscriptionId,
            @RequestParam String phoneNumber,
            @RequestParam BigDecimal amountKes) {
        Payment payment = mpesaService.initiateStkPush(
                ownerId, subscriptionId, phoneNumber, amountKes
        );
        return ResponseEntity.ok(payment);
    }

    // POST /api/v1/mpesa/callback
    // Safaricom calls this URL automatically after payment
    // This URL must be LIVE (https) when going to production
    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestBody Map<String, Object> callbackData) {
        log.info("M-Pesa callback received");
        mpesaService.handleCallback(callbackData);
        return ResponseEntity.ok("Callback processed");
    }
}