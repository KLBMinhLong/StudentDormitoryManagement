package com.dormitory.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;
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
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.CREATED.getCode(), "Tạo hợp đồng thành công", result));
    }
}