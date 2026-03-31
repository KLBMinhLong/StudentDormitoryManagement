package com.dormitory.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.utility.UtilityRecordBatchRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordDTO;
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
}
