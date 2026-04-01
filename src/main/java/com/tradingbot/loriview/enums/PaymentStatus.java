package com.tradingbot.loriview.enums;

public enum PaymentStatus {
    PENDING,    // payment initiated, waiting for M-Pesa callback
    SUCCESS,    // M-Pesa confirmed payment
    FAILED,     // M-Pesa reported failure
    CANCELLED,  // user cancelled or timed out
    REFUNDED    // payment reversed
}