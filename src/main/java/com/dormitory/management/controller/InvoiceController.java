package com.dormitory.management.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.invoice.InvoiceGenerateRequestDTO;
import com.dormitory.management.dto.invoice.InvoiceManualApproveRequestDTO;
import com.dormitory.management.dto.invoice.InvoiceResponseDTO;
import com.dormitory.management.dto.payment.CreatePaymentLinkResponseDTO;
import com.dormitory.management.dto.utility.UtilityRecordResponseDTO;
import com.dormitory.management.entity.enums.InvoiceStatus;
import com.dormitory.management.service.InvoiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/admin/utility-records")
    public ResponseEntity<ApiResponse<List<UtilityRecordResponseDTO>>> getUtilityRecordsForMonth(
            @RequestParam(required = false) Long buildingId,
            @RequestParam int month,
            @RequestParam int year) {
        List<UtilityRecordResponseDTO> result = invoiceService.getUtilityRecordsForMonth(buildingId, month, year);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách chỉ số điện nước thành công", result));
    }

    @PostMapping("/generate-monthly")
    public ResponseEntity<ApiResponse<Integer>> generateMonthlyInvoices(@Valid @RequestBody InvoiceGenerateRequestDTO request) {
        int result = invoiceService.generateMonthlyInvoices(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Sinh hóa đơn tháng thành công", result));
    }

    @PostMapping("/admin/generate-room/{roomId}")
    public ResponseEntity<ApiResponse<Integer>> generateInvoiceForRoom(
            @PathVariable Long roomId,
            @RequestParam int month,
            @RequestParam int year) {
        int result = invoiceService.generateInvoicesForRoom(roomId, month, year);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Sinh hóa đơn phòng thành công", result));
    }

    @PostMapping("/admin/mark-overdue")
    public ResponseEntity<ApiResponse<Integer>> markOverdueInvoices() {
        int result = invoiceService.markOverdueInvoices();
        return ResponseEntity.ok(ApiResponse.success(200, "Đánh dấu hóa đơn quá hạn thành công", result));
    }

    @GetMapping("/admin/list")
    public ResponseEntity<ApiResponse<PagedResponseDTO<InvoiceResponseDTO>>> searchInvoicesForAdmin(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<InvoiceResponseDTO> result = invoiceService.searchInvoicesForAdmin(
                month,
                year,
                status,
                buildingId,
                keyword,
                page,
                size,
                sortBy,
                direction);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách hóa đơn thành công", result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponseDTO<InvoiceResponseDTO>>> getMyInvoices(
            Principal principal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<InvoiceResponseDTO> result = invoiceService.getMyInvoices(
                principal.getName(),
                month,
                year,
                status,
                page,
                size,
                sortBy,
                direction);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách hóa đơn của sinh viên thành công", result));
    }

    @PostMapping("/me/{invoiceId}/create-payment-link")
    public ResponseEntity<ApiResponse<CreatePaymentLinkResponseDTO>> createPaymentLinkForMyInvoice(
            @PathVariable Long invoiceId,
            Principal principal) {
        CreatePaymentLinkResponseDTO result = invoiceService.createPaymentLinkForStudent(invoiceId, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Tạo liên kết thanh toán thành công", result));
    }

    @PostMapping("/{invoiceId}/manual-approve")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> manualApproveInvoice(
            @PathVariable Long invoiceId,
            @Valid @RequestBody InvoiceManualApproveRequestDTO request,
            Principal principal) {
        InvoiceResponseDTO result = invoiceService.manualApproveInvoice(invoiceId, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(200, "Xác nhận thanh toán thủ công thành công", result));
    }
}
