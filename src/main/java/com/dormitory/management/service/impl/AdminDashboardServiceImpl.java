package com.dormitory.management.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.dashboard.AdminDashboardOverviewDTO;
import com.dormitory.management.dto.dashboard.BuildingOccupancyItemDTO;
import com.dormitory.management.dto.dashboard.MonthlyRevenuePointDTO;
import com.dormitory.management.entity.enums.InvoiceStatus;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.repository.InvoiceRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    private final InvoiceRepository invoiceRepository;
    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

    @Override
    public AdminDashboardOverviewDTO getDashboardOverview() {
        BigDecimal totalRevenuePaid = safeMoney(invoiceRepository.sumTotalAmountByStatus(InvoiceStatus.PAID));

        LocalDate monthStartDate = LocalDate.now().withDayOfMonth(1);
        LocalDateTime monthStart = monthStartDate.atStartOfDay();
        LocalDateTime nextMonthStart = monthStartDate.plusMonths(1).atStartOfDay();
        BigDecimal revenueThisMonth = safeMoney(invoiceRepository.sumRevenueByPaidAtRange(
                InvoiceStatus.PAID,
                monthStart,
                nextMonthStart));

        long totalPaidInvoices = invoiceRepository.countByStatus(InvoiceStatus.PAID);
        long totalUnpaidInvoices = invoiceRepository.countByStatus(InvoiceStatus.UNPAID);
        long totalOverdueInvoices = invoiceRepository.countByStatus(InvoiceStatus.OVERDUE);

        long totalBeds = bedRepository.count();
        long occupiedBeds = bedRepository.countByIsOccupiedTrue();
        double bedOccupancyRate = toPercent(occupiedBeds, totalBeds);

        long totalRooms = roomRepository.count();
        long occupiedRooms = bedRepository.countDistinctOccupiedRooms();
        double roomOccupancyRate = toPercent(occupiedRooms, totalRooms);

        List<MonthlyRevenuePointDTO> revenueByMonth = buildRevenueSeries(6);
        List<BuildingOccupancyItemDTO> buildingOccupancy = buildBuildingOccupancy();

        return AdminDashboardOverviewDTO.builder()
                .totalRevenuePaid(totalRevenuePaid)
                .revenueThisMonth(revenueThisMonth)
                .totalPaidInvoices(totalPaidInvoices)
                .totalUnpaidInvoices(totalUnpaidInvoices)
                .totalOverdueInvoices(totalOverdueInvoices)
                .totalBeds(totalBeds)
                .occupiedBeds(occupiedBeds)
                .bedOccupancyRate(bedOccupancyRate)
                .totalRooms(totalRooms)
                .occupiedRooms(occupiedRooms)
                .roomOccupancyRate(roomOccupancyRate)
                .revenueByMonth(revenueByMonth)
                .buildingOccupancy(buildingOccupancy)
                .build();
    }

    private List<MonthlyRevenuePointDTO> buildRevenueSeries(int monthCount) {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(monthCount - 1L);
        LocalDateTime fromTime = startDate.atStartOfDay();
        LocalDateTime toTime = LocalDate.now().withDayOfMonth(1).plusMonths(1).atStartOfDay();

        List<Object[]> rows = invoiceRepository.aggregateRevenueByPaidMonth(InvoiceStatus.PAID, fromTime, toTime);
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : rows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal value = safeMoney((BigDecimal) row[2]);
            revenueMap.put(year + "-" + month, value);
        }

        List<MonthlyRevenuePointDTO> points = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) {
            LocalDate pointDate = startDate.plusMonths(i);
            int month = pointDate.getMonthValue();
            int year = pointDate.getYear();
            BigDecimal revenue = revenueMap.getOrDefault(year + "-" + month, BigDecimal.ZERO);
            points.add(MonthlyRevenuePointDTO.builder()
                    .month(month)
                    .year(year)
                    .periodLabel(pointDate.format(PERIOD_FORMATTER))
                    .revenue(revenue)
                    .build());
        }
        return points;
    }

    private List<BuildingOccupancyItemDTO> buildBuildingOccupancy() {
        List<Object[]> rows = buildingRepository.summarizeBedOccupancyByBuilding();
        List<BuildingOccupancyItemDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long buildingId = row[0] == null ? null : ((Number) row[0]).longValue();
            String buildingName = row[1] == null ? "-" : String.valueOf(row[1]);
            long totalBeds = row[2] == null ? 0L : ((Number) row[2]).longValue();
            long occupiedBeds = row[3] == null ? 0L : ((Number) row[3]).longValue();
            result.add(BuildingOccupancyItemDTO.builder()
                    .buildingId(buildingId)
                    .buildingName(buildingName)
                    .totalBeds(totalBeds)
                    .occupiedBeds(occupiedBeds)
                    .occupancyRate(toPercent(occupiedBeds, totalBeds))
                    .build());
        }
        return result;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private double toPercent(long part, long total) {
        if (total <= 0) {
            return 0D;
        }
        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
