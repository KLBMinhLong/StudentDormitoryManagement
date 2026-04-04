package com.dormitory.management.service;

import com.dormitory.management.dto.auth.CurrentUserResponseDTO;
import com.dormitory.management.dto.auth.LoginRequestDTO;
import com.dormitory.management.dto.auth.LoginResponseDTO;
import com.dormitory.management.dto.auth.RegisterStudentRequestDTO;
import com.dormitory.management.dto.auth.ForgotPasswordRequestDTO;
import com.dormitory.management.dto.auth.ResetPasswordRequestDTO;

public interface AuthService {

    CurrentUserResponseDTO registerStudent(RegisterStudentRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    CurrentUserResponseDTO getCurrentUser();

    boolean forgotPassword(ForgotPasswordRequestDTO request);

    void resetPassword(ResetPasswordRequestDTO request);
}
