package com.dormitory.management.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long roomId;
    private String roomNumber;
    private Long bedId;
    private int bedNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal depositAmount;
    private String status;
}