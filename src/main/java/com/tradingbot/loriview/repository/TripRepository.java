package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.enums.TripStatus;
import com.tradingbot.loriview.model.Trip;
import org.springframework.data.jpa.repository.EntityGraph; // <-- Added this import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @EntityGraph(attributePaths = {"truck"}) // Auto-fetches the truck
    List<Trip> findByTruckIdOrderByStartedAtDesc(Long truckId);

    @EntityGraph(attributePaths = {"truck"})
    List<Trip> findByDriverIdOrderByStartedAtDesc(Long driverId);

    // TripStatus enum not String
    @EntityGraph(attributePaths = {"truck"})
    Optional<Trip> findByTruckIdAndStatus(Long truckId, TripStatus status);

    // Added JOIN FETCH t.truck to the JPQL query
    @Query("""
        SELECT t FROM Trip t
        JOIN FETCH t.truck 
        WHERE t.truck.owner.id = :ownerId
        ORDER BY t.startedAt DESC
    """)
    List<Trip> findAllByOwnerId(@Param("ownerId") Long ownerId);

    // Added JOIN FETCH t.truck here as well
    @Query("""
        SELECT t FROM Trip t
        JOIN FETCH t.truck
        WHERE t.truck.id = :truckId
        AND t.startedAt BETWEEN :from AND :to
        ORDER BY t.startedAt DESC
    """)
    List<Trip> findByTruckIdAndDateRange(
            @Param("truckId") Long truckId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to
    );

    // TripStatus enum not String
    @EntityGraph(attributePaths = {"truck"})
    List<Trip> findByTruckIdAndStatusOrderByStartedAtDesc(
            Long truckId, TripStatus status);

    // Override the default findById to fetch the truck data immediately
    @EntityGraph(attributePaths = {"truck"})
    Optional<Trip> findById(Long id);

    long countByStartedAtAfter(ZonedDateTime after);
}
