package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByAccountStatus(String accountStatus);
    long countByCreatedAtAfter(ZonedDateTime after);

    List<Owner> findAllByOrderByCreatedAtDesc();
}