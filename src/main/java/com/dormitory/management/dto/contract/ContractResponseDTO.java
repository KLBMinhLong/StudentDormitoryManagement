package com.dormitory.management.dto.contract;

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
public class ContractResponseDTO {
    private Long id;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private Long roomId;
    private String roomNumber;
    private Long bedId;
    private int bedNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationMonths;
    private BigDecimal depositAmount;
    private BigDecimal monthlyRoomPrice;
    private BigDecimal totalRoomAmount;
    private String status;
    private boolean submitted;
    private LocalDateTime holdExpiresAt;
    private LocalDateTime submittedAt;
    private LocalDateTime activatedAt;
    private boolean everActivated;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String guardianName;
    private String guardianPhone;
    private String studentNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}