package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.model.LocationPing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationPingRepository extends JpaRepository<LocationPing, Long> {

    // Latest ping for a truck (live location)
    @Query("""
        SELECT lp FROM LocationPing lp
        WHERE lp.truck.id = :truckId
        ORDER BY lp.time DESC
        LIMIT 1
    """)
    Optional<LocationPing> findLatestByTruckId(@Param("truckId") Long truckId);

    // All pings for a trip (used to draw route on map)
    List<LocationPing> findByTripIdOrderByTimeAsc(Long tripId);

    // Pings for a truck within a time range
    @Query("""
        SELECT lp FROM LocationPing lp
        WHERE lp.truck.id = :truckId
        AND lp.time BETWEEN :from AND :to
        ORDER BY lp.time ASC
    """)
    List<LocationPing> findByTruckIdAndTimeRange(
            @Param("truckId") Long truckId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to
    );

    // Pings where speed exceeded a limit (for speeding reports)
    @Query("""
        SELECT lp FROM LocationPing lp
        WHERE lp.truck.id = :truckId
        AND lp.speedKmh > :speedLimit
        ORDER BY lp.time DESC
    """)
    List<LocationPing> findSpeedingByTruckId(
            @Param("truckId") Long truckId,
            @Param("speedLimit") Double speedLimit
    );
}