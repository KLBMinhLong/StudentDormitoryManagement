package com.dormitory.management.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dormitory.management.entity.enums.PaymentProvider;
import com.dormitory.management.entity.enums.InvoiceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "invoice")
public class Invoice extends BaseEntity {

    @Column(name = "invoice_code", nullable = false, length = 60)
    private String invoiceCode;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "room_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal roomFee;

    @Column(name = "electric_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal electricFee;

    @Column(name = "water_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal waterFee;

    @Column(name = "service_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceFee;

    @Column(name = "electric_usage", nullable = false, precision = 18, scale = 2)
    private BigDecimal electricUsage;

    @Column(name = "water_usage", nullable = false, precision = 18, scale = 2)
    private BigDecimal waterUsage;

    @Column(name = "electric_unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal electricUnitPrice;

    @Column(name = "water_unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal waterUnitPrice;

    @Column(name = "students_in_room", nullable = false)
    private Integer studentsInRoom;

    @Column(name = "utility_amount_per_student", nullable = false, precision = 18, scale = 2)
    private BigDecimal utilityAmountPerStudent;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "overdue_marked_at")
    private LocalDateTime overdueMarkedAt;

    @Builder.Default
    @Column(name = "paid_late", nullable = false)
    private Boolean paidLate = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false, length = 20)
    private PaymentProvider paymentProvider;

    @Column(name = "payment_order_code", length = 100)
    private String paymentOrderCode;

    @Column(name = "payment_link", length = 500)
    private String paymentLink;

    @Column(name = "payment_qr_code", length = 1000)
    private String paymentQrCode;

    @Column(name = "provider_transaction_id", length = 100)
    private String providerTransactionId;

    @Column(name = "provider_raw_payload", columnDefinition = "nvarchar(max)")
    private String providerRawPayload;

    @Column(name = "manual_approved_by", length = 100)
    private String manualApprovedBy;

    @Column(name = "manual_approved_at")
    private LocalDateTime manualApprovedAt;

    @Column(name = "manual_approval_note", columnDefinition = "nvarchar(500)")
    private String manualApprovalNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
