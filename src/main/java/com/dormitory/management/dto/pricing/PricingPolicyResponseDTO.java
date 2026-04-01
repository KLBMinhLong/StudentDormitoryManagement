package com.dormitory.management.dto.pricing;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPolicyResponseDTO {

    private Long id;
    private BigDecimal electricUnitPrice;
    private BigDecimal waterUnitPrice;
    private BigDecimal serviceFee;
    private String effectiveFrom;
    private String notes;
}
