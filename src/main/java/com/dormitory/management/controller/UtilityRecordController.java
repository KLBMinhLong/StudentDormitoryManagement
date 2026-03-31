package com.dormitory.management.controller;

import java.util.List;
import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.utility.UtilityConsumptionTimelineItemDTO;
import com.dormitory.management.dto.utility.UtilityPeriodLockRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordBatchRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordDTO;
import com.dormitory.management.dto.utility.UtilityRecordPrefillDTO;
import com.dormitory.management.dto.utility.UtilityRecordRequestDTO;
import com.dormitory.management.service.UtilityRecordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/utility-records")
public class UtilityRecordController {

    private final UtilityRecordService utilityRecordService;

    @PostMapping
    public ResponseEntity<ApiResponse<UtilityRecordDTO>> createUtilityRecord(
            @Valid @RequestBody UtilityRecordRequestDTO request) {
        UtilityRecordDTO result = utilityRecordService.createUtilityRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Thêm chỉ số điện nước thành công", result));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<UtilityRecordDTO>>> createUtilityRecordsBatch(
            @Valid @RequestBody UtilityRecordBatchRequestDTO request) {
        List<UtilityRecordDTO> result = utilityRecordService.createUtilityRecordsBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Nhập chỉ số điện nước hàng loạt thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilityRecordDTO>> getUtilityRecordById(@PathVariable Long id) {
        UtilityRecordDTO result = utilityRecordService.getUtilityRecordById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy thông tin chỉ số điện nước thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilityRecordDTO>> updateUtilityRecord(
            @PathVariable Long id,
            @Valid @RequestBody UtilityRecordRequestDTO request) {
        UtilityRecordDTO result = utilityRecordService.updateUtilityRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật chỉ số điện nước thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUtilityRecord(@PathVariable Long id) {
        utilityRecordService.deleteUtilityRecord(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(204, "Xóa chỉ số điện nước thành công", null));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagedResponseDTO<UtilityRecordDTO>>> searchUtilityRecords(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer fromMonth,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toMonth,
            @RequestParam(required = false) Integer toYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "year") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<UtilityRecordDTO> result = utilityRecordService.searchUtilityRecords(
                roomId,
                buildingId,
                month,
                year,
                fromMonth,
                fromYear,
                toMonth,
                toYear,
                page,
                size,
                sortBy,
                direction);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy lịch sử tiêu thụ điện nước thành công", result));
    }

    @GetMapping("/prefill")
    public ResponseEntity<ApiResponse<UtilityRecordPrefillDTO>> getPrefillForRoom(
            @RequestParam Long roomId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        UtilityRecordPrefillDTO result = utilityRecordService.getPrefillForRoom(roomId, month, year);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy dữ liệu gợi ý chỉ số cũ thành công", result));
    }

    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<List<UtilityConsumptionTimelineItemDTO>>> getConsumptionTimeline(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Integer fromMonth,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toMonth,
            @RequestParam(required = false) Integer toYear) {
        List<UtilityConsumptionTimelineItemDTO> result = utilityRecordService.getConsumptionTimeline(
                roomId,
                buildingId,
                fromMonth,
                fromYear,
                toMonth,
                toYear);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy biểu đồ tiêu thụ điện nước thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/me/timeline")
    public ResponseEntity<ApiResponse<List<UtilityConsumptionTimelineItemDTO>>> getMyConsumptionTimeline(
            Principal principal,
            @RequestParam(required = false) Integer fromMonth,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toMonth,
            @RequestParam(required = false) Integer toYear) {
        List<UtilityConsumptionTimelineItemDTO> result = utilityRecordService.getMyConsumptionTimeline(
                principal.getName(),
                fromMonth,
                fromYear,
                toMonth,
                toYear);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy biểu đồ tiêu thụ điện nước của sinh viên thành công", result));
    }

    @PostMapping("/period/close")
    public ResponseEntity<ApiResponse<Integer>> closePeriod(@Valid @RequestBody UtilityPeriodLockRequestDTO request) {
        int affected = utilityRecordService.closePeriod(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Chốt kỳ chỉ số điện nước thành công", affected));
    }

    @PostMapping("/period/reopen")
    public ResponseEntity<ApiResponse<Integer>> reopenPeriod(@Valid @RequestBody UtilityPeriodLockRequestDTO request) {
        int affected = utilityRecordService.reopenPeriod(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Mở khóa kỳ chỉ số điện nước thành công", affected));
    }
}
