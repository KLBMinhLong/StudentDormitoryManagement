package com.dormitory.management.service;

import com.dormitory.management.dto.pricing.PricingPolicyResponseDTO;
import com.dormitory.management.dto.pricing.PricingPolicyUpdateRequestDTO;

public interface PricingPolicyService {

    PricingPolicyResponseDTO getLatestPolicy();

    PricingPolicyResponseDTO updatePolicy(PricingPolicyUpdateRequestDTO request);
}
