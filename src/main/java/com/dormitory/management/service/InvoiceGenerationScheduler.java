package com.dormitory.management.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dormitory.management.dto.invoice.InvoiceGenerateRequestDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvoiceGenerationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceGenerationScheduler.class);

    private final InvoiceService invoiceService;

    @Scheduled(
            cron = "${app.scheduler.invoice-generation.cron:0 30 0 10 * *}",
            zone = "${app.scheduler.zone:Asia/Ho_Chi_Minh}")
    public void generateMonthlyInvoicesOnDay10() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1);

        InvoiceGenerateRequestDTO request = InvoiceGenerateRequestDTO.builder()
                .month(previousMonth.getMonthValue())
                .year(previousMonth.getYear())
                .buildingId(null)
                .build();

        try {
            int created = invoiceService.generateMonthlyInvoices(request);
            LOGGER.info("Auto-generated {} invoices for period {}/{}", created, request.getMonth(), request.getYear());
        } catch (Exception ex) {
            LOGGER.error("Failed to auto-generate invoices for period {}/{}", request.getMonth(), request.getYear(), ex);
        }
    }
}
