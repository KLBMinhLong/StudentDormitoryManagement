package com.dormitory.management.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pricing_policy")
public class PricingPolicy extends BaseEntity {

    @Column(name = "electric_unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal electricUnitPrice;

    @Column(name = "water_unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal waterUnitPrice;

    @Column(name = "service_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceFee;

    @Column(name = "effective_from", nullable = false, columnDefinition = "nvarchar(100)")
    private String effectiveFrom;

    @Column(name = "notes", columnDefinition = "nvarchar(500)")
    private String notes;
}
