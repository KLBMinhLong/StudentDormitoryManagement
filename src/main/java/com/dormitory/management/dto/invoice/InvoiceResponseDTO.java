package com.dormitory.management.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDTO {

    private Long id;
    private String invoiceCode;

    private Long studentId;
    private String studentCode;
    private String studentName;

    private Long roomId;
    private String roomNumber;
    private Long buildingId;
    private String buildingName;

    private Integer month;
    private Integer year;

    private BigDecimal roomPrice;
    private BigDecimal electricUsage;
    private BigDecimal waterUsage;
    private BigDecimal electricUnitPrice;
    private BigDecimal waterUnitPrice;
    private BigDecimal serviceFee;
    private Integer studentsInRoom;
    private BigDecimal utilityAmountPerStudent;
    private BigDecimal totalAmount;

    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime dueAt;
    private LocalDateTime paidAt;
    private Boolean paidLate;

    private String paymentProvider;
    private String paymentOrderCode;
    private String paymentLink;

    private String manualApprovedBy;
    private LocalDateTime manualApprovedAt;
    private String manualApprovalNote;
}
