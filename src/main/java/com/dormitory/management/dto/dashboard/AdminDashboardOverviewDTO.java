package com.dormitory.management.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardOverviewDTO {
    private BigDecimal totalRevenuePaid;
    private BigDecimal revenueThisMonth;
    private long totalPaidInvoices;
    private long totalUnpaidInvoices;
    private long totalOverdueInvoices;
    private long totalBeds;
    private long occupiedBeds;
    private double bedOccupancyRate;
    private long totalRooms;
    private long occupiedRooms;
    private double roomOccupancyRate;
    private List<MonthlyRevenuePointDTO> revenueByMonth;
    private List<BuildingOccupancyItemDTO> buildingOccupancy;
}
