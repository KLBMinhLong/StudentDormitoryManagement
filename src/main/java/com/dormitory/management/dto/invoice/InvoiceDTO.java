package com.dormitory.management.dto.invoice;

import java.math.BigDecimal;

import com.dormitory.management.entity.enums.InvoiceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private Long id;
    private int month;
    private int year;
    private Long roomId;
    private String roomNumber;
    private BigDecimal roomFee;
    private BigDecimal electricFee;
    private BigDecimal waterFee;
    private BigDecimal serviceFee;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
}
