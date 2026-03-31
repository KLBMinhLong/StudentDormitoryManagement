package com.dormitory.management.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.invoice.InvoiceDTO;
import com.dormitory.management.entity.Invoice;
import com.dormitory.management.entity.UtilityRecord;
import com.dormitory.management.entity.enums.InvoiceStatus;
import com.dormitory.management.entity.enums.UtilityRecordStatus;
import com.dormitory.management.exception.ResourceNotFoundException;
import com.dormitory.management.repository.InvoiceRepository;
import com.dormitory.management.repository.UtilityRecordRepository;
import com.dormitory.management.service.InvoiceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private static final BigDecimal ELECTRIC_UNIT_PRICE = BigDecimal.valueOf(3500);
    private static final BigDecimal WATER_UNIT_PRICE = BigDecimal.valueOf(20000);
    private static final BigDecimal SERVICE_FEE = BigDecimal.valueOf(50000);

    private final InvoiceRepository invoiceRepository;
    private final UtilityRecordRepository utilityRecordRepository;

    @Override
    public PagedResponseDTO<InvoiceDTO> searchInvoices(Integer month, Integer year, String status, int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        InvoiceStatus invoiceStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                invoiceStatus = InvoiceStatus.valueOf(status.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Trạng thái hóa đơn không hợp lệ: " + status);
            }
        }

        Page<InvoiceDTO> result = invoiceRepository.searchInvoices(month, year, invoiceStatus, pageable)
                .map(this::mapToDto);

        return PagedResponseDTO.fromPage(result);
    }

    @Override
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại với id: " + id));
        return mapToDto(invoice);
    }

    @Override
    @Transactional
    public List<InvoiceDTO> generateMonthlyInvoices(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ: " + month);
        }
        if (year < 2000) {
            throw new IllegalArgumentException("Năm không hợp lệ: " + year);
        }

        List<UtilityRecord> records = utilityRecordRepository.findByPeriod(null, month, year);

        List<Invoice> invoices = records.stream()
                .filter(record -> record.getPeriodStatus() == UtilityRecordStatus.CLOSED)
                .map(this::buildInvoiceFromUtilityRecord)
                .filter(record -> record != null)
                .collect(Collectors.toList());

        List<Invoice> saved = invoiceRepository.saveAll(invoices);

        return saved.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvoiceDTO markInvoiceAsPaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại với id: " + id));
        invoice.setStatus(InvoiceStatus.PAID);
        return mapToDto(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceDTO markInvoiceAsUnpaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại với id: " + id));
        invoice.setStatus(InvoiceStatus.UNPAID);
        return mapToDto(invoiceRepository.save(invoice));
    }

    private Invoice buildInvoiceFromUtilityRecord(UtilityRecord record) {
        if (record.getRoom() == null) {
            return null;
        }

        if (invoiceRepository.findByRoomIdAndMonthAndYear(record.getRoom().getId(), record.getMonth(), record.getYear()).isPresent()) {
            return null;
        }

        double electricUsage = 0;
        if (record.getOldElectric() != null && record.getNewElectric() != null) {
            electricUsage = record.getNewElectric() - record.getOldElectric();
            if (electricUsage < 0) {
                throw new IllegalStateException("Tiêu thụ điện âm cho phòng " + record.getRoom().getRoomNumber());
            }
        }

        double waterUsage = 0;
        if (record.getOldWater() != null && record.getNewWater() != null) {
            waterUsage = record.getNewWater() - record.getOldWater();
            if (waterUsage < 0) {
                throw new IllegalStateException("Tiêu thụ nước âm cho phòng " + record.getRoom().getRoomNumber());
            }
        }

        BigDecimal electricFee = BigDecimal.valueOf(electricUsage).multiply(ELECTRIC_UNIT_PRICE);
        BigDecimal waterFee = BigDecimal.valueOf(waterUsage).multiply(WATER_UNIT_PRICE);
        BigDecimal roomFee = BigDecimal.ZERO;

        BigDecimal totalAmount = electricFee.add(waterFee).add(SERVICE_FEE);

        return Invoice.builder()
                .month(record.getMonth())
                .year(record.getYear())
                .roomFee(roomFee)
                .electricFee(electricFee)
                .waterFee(waterFee)
                .serviceFee(SERVICE_FEE)
                .totalAmount(totalAmount)
                .status(InvoiceStatus.UNPAID)
                .room(record.getRoom())
                .build();
    }

    private InvoiceDTO mapToDto(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .month(invoice.getMonth())
                .year(invoice.getYear())
                .roomId(invoice.getRoom() != null ? invoice.getRoom().getId() : null)
                .roomNumber(invoice.getRoom() != null ? invoice.getRoom().getRoomNumber() : null)
                .roomFee(invoice.getRoomFee())
                .electricFee(invoice.getElectricFee())
                .waterFee(invoice.getWaterFee())
                .serviceFee(invoice.getServiceFee())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .build();
    }
}
