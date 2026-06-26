package com.tradingbot.loriview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingbot.loriview.enums.PaymentStatus;
import com.tradingbot.loriview.model.Owner;
import com.tradingbot.loriview.model.Payment;
import com.tradingbot.loriview.model.Subscription;
import com.tradingbot.loriview.repository.OwnerRepository;
import com.tradingbot.loriview.repository.PaymentRepository;
import com.tradingbot.loriview.repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaService {

    private final PaymentRepository paymentRepository;
    private final OwnerRepository ownerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // Jackson 2 mapper — used manually below

    @Value("${mpesa.consumer.key}")
    private String consumerKey;

    @Value("${mpesa.consumer.secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.callback.url}")
    private String callbackUrl;

    @Value("${mpesa.auth.url}")
    private String authUrl;

    @Value("${mpesa.stk.push.url}")
    private String stkPushUrl;

    @Value("${mpesa.stk.query.url}")
    private String stkQueryUrl;

    // Step 1 — Get OAuth access token from Safaricom
    // Receives raw String, parses manually with our own Jackson 2 ObjectMapper
    // This avoids Spring's Jackson 3 HttpMessageConverter entirely.
    private String getAccessToken() {
        String credentials = consumerKey + ":" + consumerSecret;
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Request as String, NOT JsonNode
        ResponseEntity<String> response = restTemplate.exchange(
                authUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        String rawBody = response.getBody();
        if (rawBody == null || rawBody.isBlank()) {
            throw new RuntimeException("Empty response from M-Pesa auth endpoint");
        }

        try {
            JsonNode body = objectMapper.readTree(rawBody);
            if (!body.has("access_token")) {
                throw new RuntimeException(
                        "Failed to get M-Pesa access token: " + rawBody);
            }
            return body.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse M-Pesa auth response: " + e.getMessage(), e);
        }
    }

    // Step 2 — Initiate STK Push to owner's phone
    @Transactional
    public Payment initiateStkPush(Long ownerId,
                                   Long subscriptionId,
                                   String phoneNumber,
                                   BigDecimal amountKes) {

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Owner not found: " + ownerId));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Subscription not found: " + subscriptionId));

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String rawPassword = shortcode + passkey + timestamp;
        String password = Base64.getEncoder()
                .encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));

        String formattedPhone = formatPhone(phoneNumber);

        Map<String, Object> body = new HashMap<>();
        body.put("BusinessShortCode", shortcode);
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("TransactionType", "CustomerPayBillOnline");
        body.put("Amount", amountKes.intValue());
        body.put("PartyA", formattedPhone);
        body.put("PartyB", shortcode);
        body.put("PhoneNumber", formattedPhone);
        body.put("CallBackURL", callbackUrl);
        body.put("AccountReference", "LoriView-" + ownerId);
        body.put("TransactionDesc", "LoriView Subscription Payment");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessToken());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            // Request as String, NOT JsonNode
            ResponseEntity<String> response = restTemplate.exchange(
                    stkPushUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            String rawBody = response.getBody();
            if (rawBody == null || rawBody.isBlank()) {
                throw new RuntimeException("Empty response from M-Pesa STK push");
            }

            JsonNode responseBody = objectMapper.readTree(rawBody);

            String merchantRequestId = responseBody.get("MerchantRequestID").asText();
            String checkoutRequestId = responseBody.get("CheckoutRequestID").asText();

            log.info("STK Push sent | owner: {} | checkoutRequestId: {}",
                    ownerId, checkoutRequestId);

            Payment payment = Payment.builder()
                    .owner(owner)
                    .subscription(subscription)
                    .merchantRequestId(merchantRequestId)
                    .checkoutRequestId(checkoutRequestId)
                    .phoneNumber(formattedPhone)
                    .amountKes(amountKes)
                    .status(PaymentStatus.PENDING)
                    .build();

            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("STK Push failed for owner {}: {}", ownerId, e.getMessage());
            throw new RuntimeException(
                    "Failed to initiate M-Pesa payment: " + e.getMessage());
        }
    }

    // Step 3 — Handle Safaricom callback after payment
    @Transactional
    public void handleCallback(Map<String, Object> callbackData) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = (Map<String, Object>)
                    callbackData.get("Body");

            @SuppressWarnings("unchecked")
            Map<String, Object> stkCallback = (Map<String, Object>)
                    bodyMap.get("stkCallback");

            String checkoutRequestId = stkCallback
                    .get("CheckoutRequestID").toString();
            int resultCode = Integer.parseInt(
                    stkCallback.get("ResultCode").toString());
            String resultDesc = stkCallback
                    .get("ResultDesc").toString();

            Payment payment = paymentRepository
                    .findByCheckoutRequestId(checkoutRequestId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Payment not found for checkout: " + checkoutRequestId));

            if (resultCode == 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>)
                        stkCallback.get("CallbackMetadata");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items =
                        (List<Map<String, Object>>) metadata.get("Item");

                String receiptNumber = extractMetadataValue(
                        items, "MpesaReceiptNumber");

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setMpesaReceiptNumber(receiptNumber);
                payment.setResultCode(String.valueOf(resultCode));
                payment.setResultDescription(resultDesc);
                payment.setPaidAt(ZonedDateTime.now());
                paymentRepository.save(payment);

                subscriptionService.activateSubscription(
                        payment.getSubscription().getId());

                log.info("Payment SUCCESS | receipt: {} | owner: {}",
                        receiptNumber, payment.getOwner().getId());

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setResultCode(String.valueOf(resultCode));
                payment.setResultDescription(resultDesc);
                paymentRepository.save(payment);

                log.warn("Payment FAILED | reason: {} | owner: {}",
                        resultDesc, payment.getOwner().getId());
            }

        } catch (Exception e) {
            log.error("Error processing M-Pesa callback: {}", e.getMessage());
            throw new RuntimeException(
                    "Callback processing failed: " + e.getMessage());
        }
    }

    private String formatPhone(String phone) {
        phone = phone.trim().replaceAll("\\s+", "");
        if (phone.startsWith("0")) {
            return "254" + phone.substring(1);
        }
        if (phone.startsWith("+")) {
            return phone.substring(1);
        }
        return phone;
    }

    private String extractMetadataValue(List<Map<String, Object>> items,
                                        String name) {
        return items.stream()
                .filter(item -> name.equals(item.get("Name")))
                .findFirst()
                .map(item -> item.getOrDefault("Value", "").toString())
                .orElse(null);
    }

    // Step 4 — Query Safaricom directly for real-time STK status
// Used when callback hasn't arrived yet, so frontend isn't stuck guessing
    public Map<String, Object> queryStkStatus(String checkoutRequestId) {

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String rawPassword = shortcode + passkey + timestamp;
        String password = Base64.getEncoder()
                .encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> body = new HashMap<>();
        body.put("BusinessShortCode", shortcode);
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("CheckoutRequestID", checkoutRequestId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessToken());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    stkQueryUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            String rawBody = response.getBody();
            JsonNode result = objectMapper.readTree(rawBody);

            Map<String, Object> parsed = new HashMap<>();
            parsed.put("resultCode", result.path("ResultCode").asText());
            parsed.put("resultDesc", result.path("ResultDesc").asText());
            return parsed;

        } catch (Exception e) {
            log.warn("STK status query failed for {}: {}",
                    checkoutRequestId, e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("resultCode", "PENDING");
            fallback.put("resultDesc", "Still waiting for confirmation");
            return fallback;
        }
    }

    // Step 5 — Get payment status combining DB + live Safaricom query
    public Map<String, Object> getPaymentStatus(String checkoutRequestId) {

        Payment payment = paymentRepository
                .findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found for checkout: " + checkoutRequestId));

        Map<String, Object> result = new HashMap<>();

        // If already resolved in our DB (callback already arrived), return immediately
        if (payment.getStatus() == PaymentStatus.SUCCESS
                || payment.getStatus() == PaymentStatus.FAILED) {
            result.put("status", payment.getStatus().toString());
            result.put("message", payment.getResultDescription());
            result.put("receiptNumber", payment.getMpesaReceiptNumber());
            return result;
        }

        // Still PENDING in our DB — actively query Safaricom for the latest status
        Map<String, Object> liveStatus = queryStkStatus(checkoutRequestId);
        String resultCode = String.valueOf(liveStatus.get("resultCode"));

        if ("0".equals(resultCode)) {
            // Safaricom confirms success but our callback hasn't landed yet
            result.put("status", "SUCCESS");
            result.put("message", "Payment confirmed");
        } else if ("PENDING".equals(resultCode) || "1037".equals(resultCode)) {
            // 1037 = timeout waiting for user PIN entry
            result.put("status", "PENDING");
            result.put("message", "Waiting for PIN entry on phone");
        } else if ("1032".equals(resultCode)) {
            // User cancelled the request
            result.put("status", "FAILED");
            result.put("message", "Payment was cancelled");
        } else {
            result.put("status", "PENDING");
            result.put("message", liveStatus.get("resultDesc"));
        }

        return result;
    }
}