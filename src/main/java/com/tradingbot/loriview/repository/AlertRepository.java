package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.enums.AlertType;
import com.tradingbot.loriview.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByTruckIdAndIsReadFalseOrderByTriggeredAtDesc(Long truckId);

    @Query("""
        SELECT a FROM Alert a
        WHERE a.truck.owner.id = :ownerId
        ORDER BY a.triggeredAt DESC
    """)
    List<Alert> findAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT COUNT(a) FROM Alert a
        WHERE a.truck.owner.id = :ownerId
        AND a.isRead = false
    """)
    long countUnreadByOwnerId(@Param("ownerId") Long ownerId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Alert a SET a.isRead = true
        WHERE a.truck.id = :truckId
    """)
    void markAllAsReadForTruck(@Param("truckId") Long truckId);

    // AlertType enum not String
    List<Alert> findByTruckIdAndTypeOrderByTriggeredAtDesc(
            Long truckId, AlertType type);
}