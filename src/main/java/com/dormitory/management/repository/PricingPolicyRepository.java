package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dormitory.management.entity.PricingPolicy;

@Repository
public interface PricingPolicyRepository extends JpaRepository<PricingPolicy, Long> {

    @Query(value = "SELECT TOP 1 * FROM pricing_policy ORDER BY id DESC", nativeQuery = true)
    Optional<PricingPolicy> getLatestPolicy();
}
