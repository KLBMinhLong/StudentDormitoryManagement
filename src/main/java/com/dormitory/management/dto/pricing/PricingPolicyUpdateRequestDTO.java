package com.dormitory.management.dto.pricing;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPolicyUpdateRequestDTO {

    @NotNull(message = "Giá điện không được rỗng")
    @Positive(message = "Giá điện phải lớn hơn 0")
    private BigDecimal electricUnitPrice;

    @NotNull(message = "Giá nước không được rỗng")
    @Positive(message = "Giá nước phải lớn hơn 0")
    private BigDecimal waterUnitPrice;

    @NotNull(message = "Phí dịch vụ không được rỗng")
    @Positive(message = "Phí dịch vụ phải lớn hơn 0")
    private BigDecimal serviceFee;

    private String notes;
}
