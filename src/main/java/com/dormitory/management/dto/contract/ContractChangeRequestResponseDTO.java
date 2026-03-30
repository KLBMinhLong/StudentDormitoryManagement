package com.dormitory.management.dto.contract;

import com.dormitory.management.entity.enums.ContractChangeRequestStatus;
import com.dormitory.management.entity.enums.ContractChangeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractChangeRequestResponseDTO {
    private Long id;
    private Long contractId;
    private String studentCode;
    private String studentName;
    private String roomNumber;
    private int bedNumber;
    private ContractChangeType changeType;
    private ContractChangeRequestStatus status;
    private LocalDate currentEndDate;
    private LocalDate requestedEndDate;
    private Integer extensionMonths;
    private BigDecimal additionalAmount;
    private String reason;
    private String adminNote;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private LocalDateTime createdAt;
}
