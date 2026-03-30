package com.dormitory.management.controller;

import java.io.IOException;
import java.security.Principal;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.student.ChangePasswordRequestDTO;
import com.dormitory.management.dto.student.StudentListItemDTO;
import com.dormitory.management.dto.student.StudentResidenceHistoryItemDTO;
import com.dormitory.management.dto.student.StudentDTO;
import com.dormitory.management.service.student.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentManagementController {

    private final StudentService studentService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<StudentListItemDTO>>> getAllStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<StudentListItemDTO> result = studentService.searchStudents(keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Success", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentById(@PathVariable Long id) {
        StudentDTO result = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Success", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}/residence-history")
    public ResponseEntity<ApiResponse<PagedResponseDTO<StudentResidenceHistoryItemDTO>>> getStudentResidenceHistory(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<StudentResidenceHistoryItemDTO> result = studentService.getStudentResidenceHistory(
                id, keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy lịch sử nội trú thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(@RequestBody StudentDTO request) {
        StudentDTO result = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ErrorCode.CREATED.getCode(), "Created successfully", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(@PathVariable Long id, @RequestBody StudentDTO request) {
        StudentDTO result = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Updated successfully", result));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(ErrorCode.NO_CONTENT.getCode(), "Deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<String>> adminUploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String avatarUrl = studentService.uploadAvatar(id, file);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Upload successfully", avatarUrl));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentDTO>> getMyProfile(Principal principal) {
        String username = principal.getName();
        StudentDTO result = studentService.getCurrentStudentProfile(username);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Success", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/me/residence-history")
    public ResponseEntity<ApiResponse<PagedResponseDTO<StudentResidenceHistoryItemDTO>>> getMyResidenceHistory(
            Principal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<StudentResidenceHistoryItemDTO> result = studentService.getCurrentStudentResidenceHistory(
                principal.getName(), keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Lấy lịch sử nội trú thành công", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentDTO>> updateMyProfile(Principal principal, @RequestBody StudentDTO request) {
        String username = principal.getName();
        StudentDTO result = studentService.updateCurrentStudentProfile(username, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Updated successfully", result));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(Principal principal, @RequestBody ChangePasswordRequestDTO request) {
        String username = principal.getName();
        studentService.changePassword(username, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Password changed successfully", null));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<String>> uploadMyAvatar(Principal principal, @RequestParam("file") MultipartFile file) throws IOException {
        String username = principal.getName();
        String avatarUrl = studentService.uploadMyAvatar(username, file);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Upload successfully", avatarUrl));
    }
}