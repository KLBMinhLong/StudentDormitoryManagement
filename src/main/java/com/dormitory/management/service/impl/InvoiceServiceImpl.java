package com.dormitory.management.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.invoice.InvoiceGenerateRequestDTO;
import com.dormitory.management.dto.invoice.InvoiceManualApproveRequestDTO;
import com.dormitory.management.dto.invoice.InvoiceResponseDTO;
import com.dormitory.management.dto.payment.CreatePaymentLinkResponseDTO;
import com.dormitory.management.dto.utility.UtilityRecordResponseDTO;
import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Contract;
import com.dormitory.management.entity.Invoice;
import com.dormitory.management.entity.PricingPolicy;
import com.dormitory.management.entity.Student;
import com.dormitory.management.entity.UtilityRecord;
import com.dormitory.management.entity.enums.ContractStatus;
import com.dormitory.management.entity.enums.InvoiceStatus;
import com.dormitory.management.entity.enums.PaymentProvider;
import com.dormitory.management.entity.enums.UtilityRecordStatus;
import com.dormitory.management.exception.ResourceNotFoundException;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.ContractRepository;
import com.dormitory.management.repository.InvoiceRepository;
import com.dormitory.management.repository.PricingPolicyRepository;
import com.dormitory.management.repository.UtilityRecordRepository;
import com.dormitory.management.service.InvoiceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private final InvoiceRepository invoiceRepository;
    private final UtilityRecordRepository utilityRecordRepository;
    private final ContractRepository contractRepository;
    private final AppUserRepository appUserRepository;
    private final PricingPolicyRepository pricingPolicyRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.payos.base-url:https://api-merchant.payos.vn}")
    private String payosBaseUrl;

    @Value("${app.payos.client-id:}")
    private String payosClientId;

    @Value("${app.payos.api-key:}")
    private String payosApiKey;

    @Value("${app.payos.checksum-key:}")
    private String payosChecksumKey;

    @Value("${app.payos.return-url:http://localhost:8081/user/my-invoices.html}")
    private String payosReturnUrl;

    @Value("${app.payos.cancel-url:http://localhost:8081/user/my-invoices.html}")
    private String payosCancelUrl;

    private PricingPolicy getPricingPolicy() {
        return pricingPolicyRepository.getLatestPolicy()
                .orElseGet(() -> {
                    PricingPolicy defaultPolicy = PricingPolicy.builder()
                            .electricUnitPrice(new BigDecimal("3500"))
                            .waterUnitPrice(new BigDecimal("20000"))
                            .serviceFee(new BigDecimal("50000"))
                            .effectiveFrom(LocalDate.now().toString())
                            .build();
                    return pricingPolicyRepository.save(defaultPolicy);
                });
    }

    @Override
    public List<UtilityRecordResponseDTO> getUtilityRecordsForMonth(Long buildingId, int month, int year) {
        List<UtilityRecord> records = utilityRecordRepository.findByPeriod(buildingId, month, year);
        return records.stream()
                .map(this::mapUtilityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int generateInvoicesForRoom(Long roomId, int month, int year) {
        UtilityRecord record = utilityRecordRepository.findByRoomIdAndMonthAndYear(roomId, month, year)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỉ số điện nước cho phòng và tháng này"));

        if (record.getPeriodStatus() != UtilityRecordStatus.CLOSED) {
            throw new IllegalStateException("Kỳ điện nước chưa được chốt");
        }

        List<Contract> activeContracts = contractRepository.findByRoomIdAndStatus(roomId, ContractStatus.ACTIVE);
        if (activeContracts.isEmpty()) {
            throw new IllegalStateException("Phòng không có sinh viên có hợp đồng hiệu lực");
        }

        PricingPolicy pricing = getPricingPolicy();
        BigDecimal electricUsage = toPositiveConsumption(record.getOldElectric(), record.getNewElectric());
        BigDecimal waterUsage = toPositiveConsumption(record.getOldWater(), record.getNewWater());

        BigDecimal utilityRoomTotal = electricUsage.multiply(pricing.getElectricUnitPrice())
                .add(waterUsage.multiply(pricing.getWaterUnitPrice()));

        int studentsInRoom = activeContracts.size();
        BigDecimal utilityPerStudent = utilityRoomTotal
                .divide(BigDecimal.valueOf(studentsInRoom), 2, RoundingMode.HALF_UP);

        int createdCount = 0;
        for (Contract contract : activeContracts) {
            Student student = contract.getStudent();
            if (student == null) {
            continue;
            }

            boolean exists = invoiceRepository.existsByStudentIdAndRoomIdAndMonthAndYearAndStatusNot(
                student.getId(),
                roomId,
                month,
                year,
                InvoiceStatus.CANCELLED);
            if (exists) {
            continue;
            }

            BigDecimal roomPrice = safeMoney(contract.getMonthlyRoomPrice());
            BigDecimal totalAmount = roomPrice
                .add(utilityPerStudent)
                .add(pricing.getServiceFee())
                .setScale(0, RoundingMode.HALF_UP);

            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDate dueDate = issuedAt.toLocalDate().plusDays(10);
            LocalDateTime dueAt = dueDate.atTime(23, 59, 59);

            Invoice invoice = Invoice.builder()
                .invoiceCode(buildInvoiceCode(year, month, roomId, student.getId()))
                .month(month)
                .year(year)
                .roomFee(roomPrice)
                .electricFee(electricUsage.multiply(pricing.getElectricUnitPrice()).setScale(2, RoundingMode.HALF_UP))
                .waterFee(waterUsage.multiply(pricing.getWaterUnitPrice()).setScale(2, RoundingMode.HALF_UP))
                .serviceFee(pricing.getServiceFee())
                .electricUsage(electricUsage)
                .waterUsage(waterUsage)
                .electricUnitPrice(pricing.getElectricUnitPrice())
                .waterUnitPrice(pricing.getWaterUnitPrice())
                .studentsInRoom(studentsInRoom)
                .utilityAmountPerStudent(utilityPerStudent)
                .totalAmount(totalAmount)
                .status(InvoiceStatus.UNPAID)
                .issuedAt(issuedAt)
                .dueAt(dueAt)
                .paidLate(false)
                .paymentProvider(PaymentProvider.NONE)
                .student(student)
                .room(record.getRoom())
                .build();

            invoiceRepository.save(invoice);
            createdCount++;
        }

        return createdCount;
    }

    @Override
    @Transactional
    public int generateMonthlyInvoices(InvoiceGenerateRequestDTO request) {
        List<UtilityRecord> records = utilityRecordRepository.findByPeriod(
                request.getBuildingId(),
                request.getMonth(),
                request.getYear());

        PricingPolicy pricing = getPricingPolicy();
        int createdCount = 0;

        for (UtilityRecord record : records) {
            if (record.getPeriodStatus() != UtilityRecordStatus.CLOSED) {
                continue;
            }

            List<Contract> activeContracts = contractRepository.findByRoomIdAndStatus(
                    record.getRoom().getId(),
                    ContractStatus.ACTIVE);

            if (activeContracts.isEmpty()) {
                continue;
            }

            BigDecimal electricUsage = toPositiveConsumption(record.getOldElectric(), record.getNewElectric());
            BigDecimal waterUsage = toPositiveConsumption(record.getOldWater(), record.getNewWater());

            BigDecimal utilityRoomTotal = electricUsage.multiply(pricing.getElectricUnitPrice())
                    .add(waterUsage.multiply(pricing.getWaterUnitPrice()));

            int studentsInRoom = activeContracts.size();
            BigDecimal utilityPerStudent = utilityRoomTotal
                    .divide(BigDecimal.valueOf(studentsInRoom), 2, RoundingMode.HALF_UP);

            for (Contract contract : activeContracts) {
                Student student = contract.getStudent();
                if (student == null) {
                    continue;
                }

                boolean exists = invoiceRepository.existsByStudentIdAndRoomIdAndMonthAndYearAndStatusNot(
                        student.getId(),
                        record.getRoom().getId(),
                        request.getMonth(),
                        request.getYear(),
                        InvoiceStatus.CANCELLED);
                if (exists) {
                    continue;
                }

                BigDecimal roomPrice = safeMoney(contract.getMonthlyRoomPrice());
                BigDecimal totalAmount = roomPrice
                        .add(utilityPerStudent)
                        .add(pricing.getServiceFee())
                        .setScale(0, RoundingMode.HALF_UP);

                LocalDateTime issuedAt = LocalDateTime.now();
                LocalDate dueDate = issuedAt.toLocalDate().plusDays(10);
                LocalDateTime dueAt = dueDate.atTime(23, 59, 59);

                Invoice invoice = Invoice.builder()
                        .invoiceCode(buildInvoiceCode(request.getYear(), request.getMonth(), record.getRoom().getId(), student.getId()))
                        .month(request.getMonth())
                        .year(request.getYear())
                        .roomFee(roomPrice)
                        .electricFee(electricUsage.multiply(pricing.getElectricUnitPrice()).setScale(2, RoundingMode.HALF_UP))
                        .waterFee(waterUsage.multiply(pricing.getWaterUnitPrice()).setScale(2, RoundingMode.HALF_UP))
                        .serviceFee(pricing.getServiceFee())
                        .electricUsage(electricUsage)
                        .waterUsage(waterUsage)
                        .electricUnitPrice(pricing.getElectricUnitPrice())
                        .waterUnitPrice(pricing.getWaterUnitPrice())
                        .studentsInRoom(studentsInRoom)
                        .utilityAmountPerStudent(utilityPerStudent)
                        .totalAmount(totalAmount)
                        .status(InvoiceStatus.UNPAID)
                        .issuedAt(issuedAt)
                        .dueAt(dueAt)
                        .paidLate(false)
                        .paymentProvider(PaymentProvider.NONE)
                        .student(student)
                        .room(record.getRoom())
                        .build();

                invoiceRepository.save(invoice);
                createdCount++;
            }
        }

        return createdCount;
    }

    @Override
    @Transactional
    public int markOverdueInvoices() {
        LocalDateTime now = LocalDateTime.now();
        return invoiceRepository.markOverdueInvoices(now, now);
    }

    @Override
    public PagedResponseDTO<InvoiceResponseDTO> searchInvoicesForAdmin(
            Integer month,
            Integer year,
            InvoiceStatus status,
            Long buildingId,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<InvoiceResponseDTO> result = invoiceRepository.searchInvoicesForAdmin(
                month,
                year,
                status,
                buildingId,
                keyword,
                pageable).map(this::mapToResponse);

        return PagedResponseDTO.fromPage(result);
    }

    @Override
    public PagedResponseDTO<InvoiceResponseDTO> getMyInvoices(
            String username,
            Integer month,
            Integer year,
            InvoiceStatus status,
            int page,
            int size,
            String sortBy,
            String direction) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        if (user.getStudent() == null) {
            throw new IllegalStateException("Tài khoản chưa liên kết sinh viên");
        }

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<InvoiceResponseDTO> result = invoiceRepository.searchInvoicesForStudent(
                user.getStudent().getId(),
                month,
                year,
                status,
                pageable).map(this::mapToResponse);

        return PagedResponseDTO.fromPage(result);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO manualApproveInvoice(Long invoiceId, InvoiceManualApproveRequestDTO request, String approvedBy) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại với id: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return mapToResponse(invoice);
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Hóa đơn đã hủy, không thể duyệt thủ công");
        }

        LocalDateTime now = LocalDateTime.now();
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(now);
        invoice.setPaymentProvider(PaymentProvider.MANUAL);
        invoice.setManualApprovedBy(approvedBy);
        invoice.setManualApprovedAt(now);
        invoice.setManualApprovalNote(request.getNote().trim());
        invoice.setPaidLate(invoice.getDueAt() != null && now.isAfter(invoice.getDueAt()));

        return mapToResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public CreatePaymentLinkResponseDTO createPaymentLinkForStudent(Long invoiceId, String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        if (user.getStudent() == null) {
            throw new IllegalStateException("Tài khoản chưa liên kết sinh viên");
        }

        Invoice invoice = invoiceRepository.findByIdAndStudentId(invoiceId, user.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn của sinh viên"));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Hóa đơn đã được thanh toán");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Hóa đơn đã hủy, không thể thanh toán");
        }

        ensurePayOsConfig();

        long orderCode = buildOrderCode(invoice);
        String orderCodeStr = String.valueOf(orderCode);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", invoice.getTotalAmount().setScale(0, RoundingMode.HALF_UP).intValue());
        body.put("description", buildPayOsDescription(invoice));
        body.put("returnUrl", payosReturnUrl);
        body.put("cancelUrl", payosCancelUrl);

        if (invoice.getStudent() != null) {
            body.put("buyerName", invoice.getStudent().getFullName());
            body.put("buyerEmail", invoice.getStudent().getEmail());
            body.put("buyerPhone", invoice.getStudent().getPhone());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", payosClientId);
        headers.set("x-api-key", payosApiKey);

        ResponseEntity<String> response;
        try {
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.postForEntity(
                    payosBaseUrl + "/v2/payment-requests",
                    new HttpEntity<>(body, headers),
                    String.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể kết nối PayOS để tạo link thanh toán", ex);
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(response.getBody());
        } catch (Exception ex) {
            throw new IllegalStateException("Phản hồi PayOS không hợp lệ", ex);
        }

        JsonNode dataNode = root.path("data");
        String paymentLink = pickText(dataNode, "checkoutUrl", "paymentLink");
        String qrCode = pickText(dataNode, "qrCode", "qrCodeData");

        if (paymentLink == null || paymentLink.isBlank()) {
            throw new IllegalStateException("PayOS không trả về payment link hợp lệ");
        }

        invoice.setPaymentProvider(PaymentProvider.PAYOS);
        invoice.setPaymentOrderCode(orderCodeStr);
        invoice.setPaymentLink(paymentLink);
        invoice.setPaymentQrCode(qrCode);
        invoice.setProviderRawPayload(root.toString());
        invoiceRepository.save(invoice);

        return CreatePaymentLinkResponseDTO.builder()
                .invoiceId(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .paymentOrderCode(orderCodeStr)
                .paymentLink(paymentLink)
                .paymentQrCode(qrCode)
                .provider(PaymentProvider.PAYOS.name())
                .build();
    }

    @Override
    @Transactional
    public void handlePayOsWebhook(JsonNode payload, String signatureHeader) {
        String signature = readSignature(payload, signatureHeader);
        JsonNode dataNode = payload.path("data");

        if (!verifySignature(dataNode, signature)) {
            throw new IllegalStateException("Webhook signature không hợp lệ");
        }

        if (!isPaymentSuccess(payload, dataNode)) {
            LOGGER.info("Received PayOS event but not successful payment: {}", payload);
            return;
        }

        String orderCode = textValue(dataNode, "orderCode");
        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalStateException("Webhook không có orderCode");
        }

        Invoice invoice = invoiceRepository.findByPaymentOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn theo paymentOrderCode: " + orderCode));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            LOGGER.info("Webhook idempotent: invoice {} already PAID", invoice.getId());
            return;
        }

        BigDecimal incomingAmount = new BigDecimal(textValue(dataNode, "amount", "0"));
        BigDecimal invoiceAmount = invoice.getTotalAmount().setScale(0, RoundingMode.HALF_UP);
        if (incomingAmount.compareTo(invoiceAmount) != 0) {
            throw new IllegalStateException("Số tiền webhook không khớp tổng tiền hóa đơn");
        }

        LocalDateTime now = LocalDateTime.now();
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(now);
        invoice.setPaidLate(invoice.getDueAt() != null && now.isAfter(invoice.getDueAt()));
        invoice.setPaymentProvider(PaymentProvider.PAYOS);
        invoice.setProviderTransactionId(textValue(dataNode, "reference", textValue(dataNode, "transactionId", null)));
        invoice.setProviderRawPayload(payload.toString());
        invoiceRepository.save(invoice);
    }

    private InvoiceResponseDTO mapToResponse(Invoice invoice) {
        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .studentId(invoice.getStudent() != null ? invoice.getStudent().getId() : null)
                .studentCode(invoice.getStudent() != null ? invoice.getStudent().getStudentCode() : null)
                .studentName(invoice.getStudent() != null ? invoice.getStudent().getFullName() : null)
                .roomId(invoice.getRoom() != null ? invoice.getRoom().getId() : null)
                .roomNumber(invoice.getRoom() != null ? invoice.getRoom().getRoomNumber() : null)
                .buildingId(invoice.getRoom() != null && invoice.getRoom().getBuilding() != null ? invoice.getRoom().getBuilding().getId() : null)
                .buildingName(invoice.getRoom() != null && invoice.getRoom().getBuilding() != null ? invoice.getRoom().getBuilding().getName() : null)
                .month(invoice.getMonth())
                .year(invoice.getYear())
                .roomPrice(invoice.getRoomFee())
                .electricUsage(invoice.getElectricUsage())
                .waterUsage(invoice.getWaterUsage())
                .electricUnitPrice(invoice.getElectricUnitPrice())
                .waterUnitPrice(invoice.getWaterUnitPrice())
                .serviceFee(invoice.getServiceFee())
                .studentsInRoom(invoice.getStudentsInRoom())
                .utilityAmountPerStudent(invoice.getUtilityAmountPerStudent())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                .issuedAt(invoice.getIssuedAt())
                .dueAt(invoice.getDueAt())
                .paidAt(invoice.getPaidAt())
                .paidLate(invoice.getPaidLate())
                .paymentProvider(invoice.getPaymentProvider() != null ? invoice.getPaymentProvider().name() : null)
                .paymentOrderCode(invoice.getPaymentOrderCode())
                .paymentLink(invoice.getPaymentLink())
                .manualApprovedBy(invoice.getManualApprovedBy())
                .manualApprovedAt(invoice.getManualApprovedAt())
                .manualApprovalNote(invoice.getManualApprovalNote())
                .build();
    }

    private UtilityRecordResponseDTO mapUtilityToResponse(UtilityRecord record) {
        return UtilityRecordResponseDTO.builder()
                .id(record.getId())
                .roomId(record.getRoom() != null ? record.getRoom().getId() : null)
                .roomNumber(record.getRoom() != null ? record.getRoom().getRoomNumber() : null)
                .buildingId(record.getRoom() != null && record.getRoom().getBuilding() != null ? record.getRoom().getBuilding().getId() : null)
                .buildingName(record.getRoom() != null && record.getRoom().getBuilding() != null ? record.getRoom().getBuilding().getName() : null)
                .month(record.getMonth())
                .year(record.getYear())
                .oldElectric(record.getOldElectric())
                .newElectric(record.getNewElectric())
                .oldWater(record.getOldWater())
                .newWater(record.getNewWater())
                .periodStatus(record.getPeriodStatus() != null ? record.getPeriodStatus().name() : null)
                .build();
    }

    private String buildInvoiceCode(int year, int month, Long roomId, Long studentId) {
        return String.format("INV-%d%02d-R%s-S%s", year, month, roomId, studentId);
    }

    private long buildOrderCode(Invoice invoice) {
        long base = Instant.now().getEpochSecond();
        long suffix = invoice.getId() == null ? 0 : invoice.getId() % 1000;
        return base * 1000 + suffix;
    }

    private String buildPayOsDescription(Invoice invoice) {
        return String.format("Thanh toan %s %02d/%d", invoice.getInvoiceCode(), invoice.getMonth(), invoice.getYear());
    }

    private String pickText(JsonNode node, String first, String second) {
        String primary = textValue(node, first, null);
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return textValue(node, second, null);
    }

    private String textValue(JsonNode node, String key) {
        return textValue(node, key, "");
    }

    private String textValue(JsonNode node, String key, String defaultValue) {
        if (node == null || node.isMissingNode() || node.get(key) == null || node.get(key).isNull()) {
            return defaultValue;
        }
        return node.get(key).asText(defaultValue);
    }

    private String readSignature(JsonNode payload, String signatureHeader) {
        if (signatureHeader != null && !signatureHeader.isBlank()) {
            return signatureHeader;
        }
        String bodySignature = textValue(payload, "signature", "");
        if (!bodySignature.isBlank()) {
            return bodySignature;
        }
        return textValue(payload.path("data"), "signature", "");
    }

    private boolean isPaymentSuccess(JsonNode payload, JsonNode dataNode) {
        boolean success = payload.path("success").asBoolean(false);
        String code = payload.path("code").asText("");
        String status = textValue(dataNode, "status", "");
        return success
                || "00".equals(code)
                || "PAID".equalsIgnoreCase(status)
                || "SUCCESS".equalsIgnoreCase(status);
    }

    private boolean verifySignature(JsonNode dataNode, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        String dataString = toDataString(dataNode);
        String expected = hmacSha256(dataString, payosChecksumKey);
        return expected.equalsIgnoreCase(signature);
    }

    private String toDataString(JsonNode node) {
        if (node == null || node.isMissingNode() || !node.isObject()) {
            return "";
        }
        List<String> keys = new ArrayList<>();
        Iterator<String> fields = node.fieldNames();
        while (fields.hasNext()) {
            keys.add(fields.next());
        }
        keys.sort(Comparator.naturalOrder());

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            JsonNode valueNode = node.get(key);
            String value;
            if (valueNode == null || valueNode.isNull()) {
                value = "";
            } else if (valueNode.isValueNode()) {
                value = valueNode.asText("");
            } else {
                value = valueNode.toString();
            }

            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(key).append('=').append(value);
        }
        return sb.toString();
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tính chữ ký HMAC SHA256", ex);
        }
    }

    private void ensurePayOsConfig() {
        if (payosClientId == null || payosClientId.isBlank()
                || payosApiKey == null || payosApiKey.isBlank()
                || payosChecksumKey == null || payosChecksumKey.isBlank()) {
            throw new IllegalStateException("Thiếu cấu hình PayOS (client-id/api-key/checksum-key)");
        }
    }

    private BigDecimal toPositiveConsumption(Double oldValue, Double newValue) {
        BigDecimal oldNum = BigDecimal.valueOf(oldValue == null ? 0D : oldValue);
        BigDecimal newNum = BigDecimal.valueOf(newValue == null ? 0D : newValue);
        if (newNum.compareTo(oldNum) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return newNum.subtract(oldNum).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
