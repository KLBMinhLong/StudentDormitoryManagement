package com.dormitory.management.dto.student;

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
public class StudentResidenceHistoryItemDTO {
    private Long contractId;
    private Long roomId;
    private String buildingName;
    private String roomNumber;
    private Long bedId;
    private Integer bedNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationMonths;
    private String status;
    private LocalDateTime activatedAt;
    private LocalDateTime createdAt;
}
