package com.dormitory.management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.auth.CurrentUserResponseDTO;
import com.dormitory.management.dto.auth.LoginRequestDTO;
import com.dormitory.management.dto.auth.LoginResponseDTO;
import com.dormitory.management.dto.auth.RegisterStudentRequestDTO;
import com.dormitory.management.dto.auth.ForgotPasswordRequestDTO;
import com.dormitory.management.dto.auth.ResetPasswordRequestDTO;
import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<CurrentUserResponseDTO>> registerStudent(
            @Valid @RequestBody RegisterStudentRequestDTO request) {
        CurrentUserResponseDTO result = authService.registerStudent(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(ErrorCode.CREATED.getCode(), "Register student successfully", result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Login successfully", result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponseDTO>> getCurrentUser() {
        CurrentUserResponseDTO result = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get current user successfully", result));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), 
            "Hướng dẫn đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra email của bạn.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), 
            "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập lại.", null));
    }
}
