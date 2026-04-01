package com.tradingbot.loriview.enums;

public enum AlertType {
    SPEEDING,        // truck exceeded speed limit
    GEOFENCE,        // truck left assigned route or zone
    FUEL_DROP,       // sudden fuel level drop (possible theft)
    IDLE,            // truck has been stationary too long
    OFFLINE,         // tracker stopped sending data
    HARSH_BRAKING,   // sudden hard brake detected
    ACCELERATION     // sudden hard acceleration detected
}