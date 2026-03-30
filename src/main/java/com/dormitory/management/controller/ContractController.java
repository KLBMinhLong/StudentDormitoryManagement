package com.dormitory.management.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractReservationRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;
import com.dormitory.management.dto.contract.ContractSubmitRequestDTO;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.service.ContractService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ContractResponseDTO>> createContract(@Valid @RequestBody ContractRequestDTO request) {
        ContractResponseDTO result = contractService.createContract(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(ErrorCode.CREATED.getCode(), "Tạo hợp đồng thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PostMapping("/reservations")
    public ResponseEntity<ApiResponse<ContractResponseDTO>> reserveBed(
            Principal principal,
            @Valid @RequestBody ContractReservationRequestDTO request) {
        ContractResponseDTO result = contractService.reserveBedForCurrentStudent(principal.getName(), request);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(ErrorCode.CREATED.getCode(), "Giữ chỗ giường thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PutMapping("/{contractId}/submit")
    public ResponseEntity<ApiResponse<ContractResponseDTO>> submitContractProfile(
            Principal principal,
            @PathVariable Long contractId,
            @Valid @RequestBody ContractSubmitRequestDTO request) {
        ContractResponseDTO result = contractService.submitContractProfile(principal.getName(), contractId, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Nộp hồ sơ hợp đồng thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/me/pending")
    public ResponseEntity<ApiResponse<ContractResponseDTO>> getMyPendingContract(Principal principal) {
        ContractResponseDTO result = contractService.getMyPendingContract(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy yêu cầu hợp đồng thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{contractId}/approve")
    public ResponseEntity<ApiResponse<ContractResponseDTO>> approvePendingContract(@PathVariable Long contractId) {
        ContractResponseDTO result = contractService.approvePendingContract(contractId);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Duyệt hợp đồng thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{contractId}/reject")
    public ResponseEntity<ApiResponse<ContractResponseDTO>> rejectPendingContract(
            @PathVariable Long contractId,
            @RequestParam(required = false) String reason) {
        ContractResponseDTO result = contractService.rejectPendingContract(contractId, reason);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Từ chối hợp đồng thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ContractResponseDTO>>> getPendingContractsForAdmin() {
        List<ContractResponseDTO> result = contractService.getPendingContractsForAdmin();
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy danh sách chờ duyệt thành công", result));
    }
}