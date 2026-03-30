package com.dormitory.management.dto.auth;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterStudentRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Student code is required")
    @Size(max = 50, message = "Student code must not exceed 50 characters")
    private String studentCode;

    @NotBlank(message = "CCCD is required")
    @Size(max = 20, message = "CCCD must not exceed 20 characters")
    private String cccd;

    @Email(message = "Email is invalid")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    private LocalDate dateOfBirth;
}
