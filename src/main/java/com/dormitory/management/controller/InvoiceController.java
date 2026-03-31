package com.dormitory.management.controller;

import java.util.List;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.invoice.InvoiceDTO;
import com.dormitory.management.service.InvoiceService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ACCOUNTANT')")
    @PostMapping("/utility-billing/generate-monthly")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> generateMonthlyInvoices(
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam int year) {
        List<InvoiceDTO> result = invoiceService.generateMonthlyInvoices(month, year);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "Sinh hóa đơn utilities thành công", result));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_ACCOUNTANT')")
    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<PagedResponseDTO<InvoiceDTO>>> searchInvoices(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<InvoiceDTO> result = invoiceService.searchInvoices(month, year, status, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách hóa đơn thành công", result));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_ACCOUNTANT')")
    @GetMapping("/invoices/{id}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoiceById(@PathVariable Long id) {
        InvoiceDTO result = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy hóa đơn thành công", result));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_ACCOUNTANT')")
    @PutMapping("/invoices/{id}/mark-paid")
    public ResponseEntity<ApiResponse<InvoiceDTO>> markInvoicePaid(@PathVariable Long id) {
        InvoiceDTO result = invoiceService.markInvoiceAsPaid(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái hóa đơn thành PAID", result));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_ACCOUNTANT')")
    @PutMapping("/invoices/{id}/mark-unpaid")
    public ResponseEntity<ApiResponse<InvoiceDTO>> markInvoiceUnpaid(@PathVariable Long id) {
        InvoiceDTO result = invoiceService.markInvoiceAsUnpaid(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái hóa đơn thành UNPAID", result));
    }
}
