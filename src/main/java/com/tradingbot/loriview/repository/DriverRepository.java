package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByOwnerId(Long ownerId);

    Optional<Driver> findByLicenceNo(String licenceNo);

    boolean existsByLicenceNo(String licenceNo);
}