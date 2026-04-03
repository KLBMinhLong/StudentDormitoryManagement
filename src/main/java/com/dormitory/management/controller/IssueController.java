package com.dormitory.management.controller;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.issue.IssueRequestDTO;
import com.dormitory.management.dto.issue.IssueResponseDTO;
import com.dormitory.management.dto.issue.IssueStatusUpdateDTO;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PostMapping
    public ResponseEntity<ApiResponse<IssueResponseDTO>> createIssue(Principal principal, @Valid @RequestBody IssueRequestDTO request) {
        IssueResponseDTO result = issueService.createIssue(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ErrorCode.CREATED.getCode(), "Gửi yêu cầu sửa chữa thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<IssueResponseDTO>>> getMyIssues(Principal principal) {
        List<IssueResponseDTO> result = issueService.getMyIssues(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy danh sách yêu cầu thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<IssueResponseDTO>>> getAllIssues() {
        List<IssueResponseDTO> result = issueService.getAllIssues();
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy danh sách yêu cầu thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IssueResponseDTO>> updateIssueStatus(@PathVariable Long id, @RequestBody IssueStatusUpdateDTO request) {
        IssueResponseDTO result = issueService.updateIssueStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Cập nhật trạng thái thành công", result));
    }
}
