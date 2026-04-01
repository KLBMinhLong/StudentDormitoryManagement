package com.dormitory.management.service;

import java.util.List;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.invoice.InvoiceGenerateRequestDTO;
import com.dormitory.management.dto.invoice.InvoiceManualApproveRequestDTO;
import com.dormitory.management.dto.invoice.InvoiceResponseDTO;
import com.dormitory.management.dto.payment.CreatePaymentLinkResponseDTO;
import com.dormitory.management.dto.utility.UtilityRecordResponseDTO;
import com.dormitory.management.entity.enums.InvoiceStatus;
import com.fasterxml.jackson.databind.JsonNode;

public interface InvoiceService {

    List<UtilityRecordResponseDTO> getUtilityRecordsForMonth(Long buildingId, int month, int year);

    int generateMonthlyInvoices(InvoiceGenerateRequestDTO request);

        int generateInvoicesForRoom(Long roomId, int month, int year);

    int markOverdueInvoices();

    PagedResponseDTO<InvoiceResponseDTO> searchInvoicesForAdmin(
            Integer month,
            Integer year,
            InvoiceStatus status,
            Long buildingId,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction);

    PagedResponseDTO<InvoiceResponseDTO> getMyInvoices(
            String username,
            Integer month,
            Integer year,
            InvoiceStatus status,
            int page,
            int size,
            String sortBy,
            String direction);

    InvoiceResponseDTO manualApproveInvoice(Long invoiceId, InvoiceManualApproveRequestDTO request, String approvedBy);

    CreatePaymentLinkResponseDTO createPaymentLinkForStudent(Long invoiceId, String username);

    void handlePayOsWebhook(JsonNode payload, String signatureHeader);
}
