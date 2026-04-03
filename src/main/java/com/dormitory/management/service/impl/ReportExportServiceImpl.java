package com.dormitory.management.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dormitory.management.dto.contract.ContractResponseDTO;
import com.dormitory.management.dto.invoice.InvoiceResponseDTO;
import com.dormitory.management.dto.utility.UtilityRecordDTO;
import com.dormitory.management.entity.Student;
import com.dormitory.management.entity.enums.ContractStatus;
import com.dormitory.management.entity.enums.InvoiceStatus;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.StudentRepository;
import com.dormitory.management.service.ContractService;
import com.dormitory.management.service.InvoiceService;
import com.dormitory.management.service.ReportExportService;
import com.dormitory.management.service.UtilityRecordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportExportServiceImpl implements ReportExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;
    private final ContractService contractService;
    private final InvoiceService invoiceService;
    private final UtilityRecordService utilityRecordService;

    @Override
    public byte[] exportStudentsExcel(String keyword, String sortBy, String direction, boolean residentOnly) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = StringUtils.hasText(sortBy) ? sortBy : "id";
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        Page<Student> page;
        if (residentOnly) {
            page = studentRepository.searchResidentStudents(
                    normalizedKeyword,
                    ContractStatus.ACTIVE,
                    PageRequest.of(0, 5000, Sort.by(sortDirection, sortField)));
        } else {
            page = StringUtils.hasText(normalizedKeyword)
                    ? studentRepository.searchByCodeOrName(normalizedKeyword, PageRequest.of(0, 5000, Sort.by(sortDirection, sortField)))
                    : studentRepository.findAll(PageRequest.of(0, 5000, Sort.by(sortDirection, sortField)));
        }

        List<Student> rows = page.getContent();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sinh vien");
        writeHeader(sheet, new String[] {
                "ID", "Mã SV", "Họ tên", "Giới tính", "SĐT", "Email", "Tài khoản", "Ngày sinh"
        });

        int rowIndex = 1;
        for (Student student : rows) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(student.getId() == null ? 0 : student.getId());
            row.createCell(1).setCellValue(stringValue(student.getStudentCode()));
            row.createCell(2).setCellValue(stringValue(student.getFullName()));
            row.createCell(3).setCellValue(stringValue(student.getGender()));
            row.createCell(4).setCellValue(stringValue(student.getPhone()));
            row.createCell(5).setCellValue(stringValue(student.getEmail()));
            row.createCell(6).setCellValue(appUserRepository.findByStudentId(student.getId()).map(x -> x.getUsername()).orElse(""));
            row.createCell(7).setCellValue(formatDate(student.getDateOfBirth()));
        }

        autosize(sheet, 8);
        return toBytes(workbook);
    }

    @Override
    public byte[] exportContractsExcel(String status, String keyword, String occupancyType, String sortBy, String direction) {
        List<ContractResponseDTO> rows = contractService.getContractsForAdmin(
                status,
                keyword,
                occupancyType,
                0,
                5000,
                sortBy,
                direction).getContent();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Hop dong");
        writeHeader(sheet, new String[] {
                "Mã HĐ", "Mã SV", "Sinh viên", "Phòng", "Giường", "Trạng thái", "Bắt đầu", "Kết thúc", "Tiền cọc", "Tiền phòng/tháng"
        });

        int rowIndex = 1;
        for (ContractResponseDTO item : rows) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getId() == null ? 0 : item.getId());
            row.createCell(1).setCellValue(stringValue(item.getStudentCode()));
            row.createCell(2).setCellValue(stringValue(item.getStudentName()));
            row.createCell(3).setCellValue(stringValue(item.getRoomNumber()));
            row.createCell(4).setCellValue(item.getBedNumber());
            row.createCell(5).setCellValue(stringValue(item.getStatus()));
            row.createCell(6).setCellValue(formatDate(item.getStartDate()));
            row.createCell(7).setCellValue(formatDate(item.getEndDate()));
            row.createCell(8).setCellValue(numberValue(item.getDepositAmount()));
            row.createCell(9).setCellValue(numberValue(item.getMonthlyRoomPrice()));
        }

        autosize(sheet, 10);
        return toBytes(workbook);
    }

    @Override
    public byte[] exportInvoicesExcel(
            Integer month,
            Integer year,
            InvoiceStatus status,
            Long buildingId,
            String keyword,
            String sortBy,
            String direction) {
        List<InvoiceResponseDTO> rows = invoiceService.searchInvoicesForAdmin(
                month,
                year,
                status,
                buildingId,
                keyword,
                0,
                5000,
                sortBy,
                direction).getContent();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Hoa don");
        writeHeader(sheet, new String[] {
                "Mã HĐ", "Mã SV", "Sinh viên", "Tòa", "Phòng", "Kỳ", "Tổng tiền", "Trạng thái", "Nguồn TT", "Hạn thanh toán"
        });

        int rowIndex = 1;
        for (InvoiceResponseDTO item : rows) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(stringValue(item.getInvoiceCode()));
            row.createCell(1).setCellValue(stringValue(item.getStudentCode()));
            row.createCell(2).setCellValue(stringValue(item.getStudentName()));
            row.createCell(3).setCellValue(stringValue(item.getBuildingName()));
            row.createCell(4).setCellValue(stringValue(item.getRoomNumber()));
            row.createCell(5).setCellValue(String.format("%s/%s", item.getMonth(), item.getYear()));
            row.createCell(6).setCellValue(numberValue(item.getTotalAmount()));
            row.createCell(7).setCellValue(stringValue(item.getStatus()));
            row.createCell(8).setCellValue(stringValue(item.getPaymentProvider()));
            row.createCell(9).setCellValue(formatDateTime(item.getDueAt()));
        }

        autosize(sheet, 10);
        return toBytes(workbook);
    }

    @Override
    public byte[] exportUtilityRecordsExcel(
            Long buildingId,
            Integer month,
            Integer year,
            Integer fromMonth,
            Integer fromYear,
            Integer toMonth,
            Integer toYear,
            String sortBy,
            String direction) {
        List<UtilityRecordDTO> rows = utilityRecordService.searchUtilityRecords(
                null,
                buildingId,
                month,
                year,
                fromMonth,
                fromYear,
                toMonth,
                toYear,
                0,
                5000,
                sortBy,
                direction).getContent();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Chi so dien nuoc");
        writeHeader(sheet, new String[] {
                "ID", "Tòa", "Phòng", "Tháng", "Năm", "Điện cũ", "Điện mới", "Nước cũ", "Nước mới", "Trạng thái"
        });

        int rowIndex = 1;
        for (UtilityRecordDTO item : rows) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getId() == null ? 0 : item.getId());
            row.createCell(1).setCellValue(stringValue(item.getBuildingName()));
            row.createCell(2).setCellValue(stringValue(item.getRoomNumber()));
            row.createCell(3).setCellValue(item.getMonth() == null ? 0 : item.getMonth());
            row.createCell(4).setCellValue(item.getYear() == null ? 0 : item.getYear());
            row.createCell(5).setCellValue(item.getOldElectric() == null ? 0 : item.getOldElectric());
            row.createCell(6).setCellValue(item.getNewElectric() == null ? 0 : item.getNewElectric());
            row.createCell(7).setCellValue(item.getOldWater() == null ? 0 : item.getOldWater());
            row.createCell(8).setCellValue(item.getNewWater() == null ? 0 : item.getNewWater());
            row.createCell(9).setCellValue(stringValue(item.getPeriodStatus()));
        }

        autosize(sheet, 10);
        return toBytes(workbook);
    }

    private void writeHeader(Sheet sheet, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }

    private String stringValue(String value) {
        return value == null ? "" : value;
    }

    private double numberValue(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATETIME_FORMAT);
    }

    private void autosize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] toBytes(Workbook workbook) {
        try (workbook; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo file Excel", ex);
        }
    }
}
