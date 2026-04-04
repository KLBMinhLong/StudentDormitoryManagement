package com.dormitory.management.service.impl;

import static com.dormitory.management.config.CacheConfig.PRICING_LATEST_CACHE;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.pricing.PricingPolicyResponseDTO;
import com.dormitory.management.dto.pricing.PricingPolicyUpdateRequestDTO;
import com.dormitory.management.entity.PricingPolicy;
import com.dormitory.management.repository.PricingPolicyRepository;
import com.dormitory.management.service.PricingPolicyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PricingPolicyServiceImpl implements PricingPolicyService {

    private final PricingPolicyRepository pricingPolicyRepository;

    @Override
    @Cacheable(cacheNames = PRICING_LATEST_CACHE)
    public PricingPolicyResponseDTO getLatestPolicy() {
        PricingPolicy policy = pricingPolicyRepository.findTopByOrderByIdDesc()
                .orElseGet(() -> {
                    PricingPolicy defaultPolicy = PricingPolicy.builder()
                            .electricUnitPrice(new BigDecimal("3500"))
                            .waterUnitPrice(new BigDecimal("20000"))
                            .serviceFee(new BigDecimal("50000"))
                            .effectiveFrom(LocalDate.now().toString())
                            .notes("Giá mặc định hệ thống")
                            .build();
                    return pricingPolicyRepository.save(defaultPolicy);
                });

        return mapToResponse(policy);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = PRICING_LATEST_CACHE, allEntries = true)
    public PricingPolicyResponseDTO updatePolicy(PricingPolicyUpdateRequestDTO request) {
        PricingPolicy policy = PricingPolicy.builder()
                .electricUnitPrice(request.getElectricUnitPrice())
                .waterUnitPrice(request.getWaterUnitPrice())
                .serviceFee(request.getServiceFee())
                .effectiveFrom(LocalDate.now().toString())
                .notes(request.getNotes())
                .build();

        return mapToResponse(pricingPolicyRepository.save(policy));
    }

    private PricingPolicyResponseDTO mapToResponse(PricingPolicy policy) {
        return PricingPolicyResponseDTO.builder()
                .id(policy.getId())
                .electricUnitPrice(policy.getElectricUnitPrice())
                .waterUnitPrice(policy.getWaterUnitPrice())
                .serviceFee(policy.getServiceFee())
                .effectiveFrom(policy.getEffectiveFrom())
                .notes(policy.getNotes())
                .build();
    }
}
