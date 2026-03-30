package com.dormitory.management.dto.contract;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractSubmitRequestDTO {

    @NotNull(message = "Duration months is required")
    @Min(value = 6, message = "Duration must be 6 or 12 months")
    @Max(value = 12, message = "Duration must be 6 or 12 months")
    private Integer durationMonths;

    @NotNull(message = "Deposit amount is required")
    private BigDecimal depositAmount;

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    private String emergencyContactPhone;

    @NotBlank(message = "Guardian name is required")
    private String guardianName;

    @NotBlank(message = "Guardian phone is required")
    private String guardianPhone;

    private String studentNote;
}
