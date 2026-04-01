package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.model.FuelReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FuelReadingRepository extends JpaRepository<FuelReading, Long> {

    // Latest fuel reading for a truck (current fuel level)
    @Query("""
        SELECT f FROM FuelReading f
        WHERE f.truck.id = :truckId
        ORDER BY f.time DESC
        LIMIT 1
    """)
    Optional<FuelReading> findLatestByTruckId(@Param("truckId") Long truckId);

    // All readings for a trip (to calculate total fuel used)
    List<FuelReading> findByTripIdOrderByTimeAsc(Long tripId);

    // Fuel readings within a time range
    @Query("""
        SELECT f FROM FuelReading f
        WHERE f.truck.id = :truckId
        AND f.time BETWEEN :from AND :to
        ORDER BY f.time ASC
    """)
    List<FuelReading> findByTruckIdAndTimeRange(
            @Param("truckId") Long truckId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to
    );
}