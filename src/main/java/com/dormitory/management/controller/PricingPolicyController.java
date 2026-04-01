package com.dormitory.management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.pricing.PricingPolicyResponseDTO;
import com.dormitory.management.dto.pricing.PricingPolicyUpdateRequestDTO;
import com.dormitory.management.service.PricingPolicyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pricing-policies")
@RequiredArgsConstructor
public class PricingPolicyController {

    private final PricingPolicyService pricingPolicyService;

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<PricingPolicyResponseDTO>> getLatestPolicy() {
        PricingPolicyResponseDTO policy = pricingPolicyService.getLatestPolicy();
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy giá dịch vụ thành công", policy));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PricingPolicyResponseDTO>> updatePolicy(
            @Valid @RequestBody PricingPolicyUpdateRequestDTO request) {
        PricingPolicyResponseDTO updated = pricingPolicyService.updatePolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Cập nhật giá dịch vụ thành công", updated));
    }
}
