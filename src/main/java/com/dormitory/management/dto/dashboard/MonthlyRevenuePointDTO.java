package com.dormitory.management.dto.dashboard;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenuePointDTO {
    private int month;
    private int year;
    private String periodLabel;
    private BigDecimal revenue;
}
