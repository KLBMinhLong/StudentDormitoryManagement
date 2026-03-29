package com.dormitory.management.service;

import com.dormitory.management.dto.auth.CurrentUserResponseDTO;
import com.dormitory.management.dto.auth.LoginRequestDTO;
import com.dormitory.management.dto.auth.LoginResponseDTO;
import com.dormitory.management.dto.auth.RegisterStudentRequestDTO;

public interface AuthService {

    CurrentUserResponseDTO registerStudent(RegisterStudentRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    CurrentUserResponseDTO getCurrentUser();
}
