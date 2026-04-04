package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dormitory.management.entity.PricingPolicy;

@Repository
public interface PricingPolicyRepository extends JpaRepository<PricingPolicy, Long> {

    Optional<PricingPolicy> findTopByOrderByIdDesc();
}
