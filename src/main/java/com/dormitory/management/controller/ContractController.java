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
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractReservationRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;
import com.dormitory.management.dto.contract.ContractSubmitRequestDTO;
import com.dormitory.management.dto.contract.ContractChangeRequestCreateDTO;
import com.dormitory.management.dto.contract.ContractChangeRequestResponseDTO;
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

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/me/contracts")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ContractResponseDTO>>> getMyContracts(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        PagedResponseDTO<ContractResponseDTO> result = contractService.getMyContracts(
                principal.getName(), status, keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy danh sách hợp đồng thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/me/contracts/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponseDTO>> getMyContractDetail(
            Principal principal,
            @PathVariable Long contractId) {
        ContractResponseDTO result = contractService.getMyContractDetail(principal.getName(), contractId);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy chi tiết hợp đồng thành công", result));
    }

        @PreAuthorize("hasAuthority('ROLE_STUDENT')")
        @PostMapping("/{contractId}/change-requests")
        public ResponseEntity<ApiResponse<ContractChangeRequestResponseDTO>> createMyContractChangeRequest(
            Principal principal,
            @PathVariable Long contractId,
            @Valid @RequestBody ContractChangeRequestCreateDTO request) {
        ContractChangeRequestResponseDTO result = contractService.createMyContractChangeRequest(
            principal.getName(), contractId, request);
        return ResponseEntity.status(201)
            .body(ApiResponse.success(ErrorCode.CREATED.getCode(), "Gửi yêu cầu thay đổi hợp đồng thành công", result));
        }

        @PreAuthorize("hasAuthority('ROLE_STUDENT')")
        @GetMapping("/me/change-requests")
        public ResponseEntity<ApiResponse<PagedResponseDTO<ContractChangeRequestResponseDTO>>> getMyContractChangeRequests(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String keyword) {
        PagedResponseDTO<ContractChangeRequestResponseDTO> result = contractService.getMyContractChangeRequests(
            principal.getName(), status, changeType, keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy danh sách yêu cầu thay đổi thành công", result));
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

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/management")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ContractResponseDTO>>> getContractsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        PagedResponseDTO<ContractResponseDTO> result = contractService.getContractsForAdmin(
                status, keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy danh sách hợp đồng thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponseDTO>> getContractDetailForAdmin(@PathVariable Long contractId) {
        ContractResponseDTO result = contractService.getContractDetailForAdmin(contractId);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy chi tiết hợp đồng thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{contractId}/cancel-early")
    public ResponseEntity<ApiResponse<ContractResponseDTO>> cancelContractEarlyByAdmin(
            @PathVariable Long contractId,
            @RequestParam(required = false) String reason) {
        ContractResponseDTO result = contractService.cancelContractEarlyByAdmin(contractId, reason);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Hủy hợp đồng sớm thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/change-requests")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ContractChangeRequestResponseDTO>>> getContractChangeRequestsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String keyword) {
        PagedResponseDTO<ContractChangeRequestResponseDTO> result = contractService.getContractChangeRequestsForAdmin(
                status, changeType, keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy danh sách yêu cầu thay đổi thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/change-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<ContractChangeRequestResponseDTO>> approveContractChangeRequest(
            Principal principal,
            @PathVariable Long requestId,
            @RequestParam(required = false) String adminNote) {
        ContractChangeRequestResponseDTO result = contractService.approveContractChangeRequest(
                requestId, principal.getName(), adminNote);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Duyệt yêu cầu thay đổi thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/change-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<ContractChangeRequestResponseDTO>> rejectContractChangeRequest(
            Principal principal,
            @PathVariable Long requestId,
            @RequestParam(required = false) String adminNote) {
        ContractChangeRequestResponseDTO result = contractService.rejectContractChangeRequest(
                requestId, principal.getName(), adminNote);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Từ chối yêu cầu thay đổi thành công", result));
    }
}