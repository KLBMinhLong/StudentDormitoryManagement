package com.dormitory.management.service;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.invoice.InvoiceDTO;

import java.util.List;

public interface InvoiceService {

    PagedResponseDTO<InvoiceDTO> searchInvoices(Integer month, Integer year, String status, int page, int size, String sortBy, String direction);

    InvoiceDTO getInvoiceById(Long id);

    List<InvoiceDTO> generateMonthlyInvoices(int month, int year);

    InvoiceDTO markInvoiceAsPaid(Long id);

    InvoiceDTO markInvoiceAsUnpaid(Long id);
}
