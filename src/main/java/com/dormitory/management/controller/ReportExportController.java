package com.dormitory.management.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.entity.enums.InvoiceStatus;
import com.dormitory.management.service.ReportExportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports/admin")
@RequiredArgsConstructor
public class ReportExportController {

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReportExportService reportExportService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/students/excel")
    public ResponseEntity<byte[]> exportStudentsExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "false") boolean residentOnly) {
        byte[] bytes = reportExportService.exportStudentsExcel(keyword, sortBy, direction, residentOnly);
        return buildExcelResponse(bytes, "danh-sach-sinh-vien");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/contracts/excel")
    public ResponseEntity<byte[]> exportContractsExcel(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String occupancyType,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        byte[] bytes = reportExportService.exportContractsExcel(status, keyword, occupancyType, sortBy, direction);
        return buildExcelResponse(bytes, "danh-sach-hop-dong");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/invoices/excel")
    public ResponseEntity<byte[]> exportInvoicesExcel(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        byte[] bytes = reportExportService.exportInvoicesExcel(month, year, status, buildingId, keyword, sortBy, direction);
        return buildExcelResponse(bytes, "danh-sach-hoa-don");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/utility-records/excel")
    public ResponseEntity<byte[]> exportUtilityRecordsExcel(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer fromMonth,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toMonth,
            @RequestParam(required = false) Integer toYear,
            @RequestParam(defaultValue = "year") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        byte[] bytes = reportExportService.exportUtilityRecordsExcel(
                buildingId,
                month,
                year,
                fromMonth,
                fromYear,
                toMonth,
                toYear,
                sortBy,
                direction);
        return buildExcelResponse(bytes, "chi-so-dien-nuoc");
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] bytes, String prefix) {
        String filename = String.format("%s_%s.xlsx", prefix, LocalDateTime.now().format(FILE_TIME));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(EXCEL_MEDIA_TYPE);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
