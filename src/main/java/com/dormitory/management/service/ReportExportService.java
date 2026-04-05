package com.dormitory.management.service;

import com.dormitory.management.entity.enums.InvoiceStatus;

public interface ReportExportService {

        byte[] exportStudentsExcel(String keyword, String sortBy, String direction, boolean residentOnly);

    byte[] exportContractsExcel(String status, String keyword, String occupancyType, String sortBy, String direction);

    byte[] exportInvoicesExcel(
            Integer month,
            Integer year,
            InvoiceStatus status,
            Long buildingId,
            String keyword,
            String sortBy,
            String direction);

    byte[] exportUtilityRecordsExcel(
            Long buildingId,
            Integer month,
            Integer year,
            Integer fromMonth,
            Integer fromYear,
            Integer toMonth,
            Integer toYear,
            String sortBy,
            String direction);
}
