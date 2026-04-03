package com.dormitory.management.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dormitory.management.dto.auth.CurrentUserResponseDTO;
import com.dormitory.management.dto.auth.LoginRequestDTO;
import com.dormitory.management.dto.auth.LoginResponseDTO;
import com.dormitory.management.dto.auth.RegisterStudentRequestDTO;
import com.dormitory.management.dto.auth.ForgotPasswordRequestDTO;
import com.dormitory.management.dto.auth.ResetPasswordRequestDTO;
import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.PasswordResetToken;
import com.dormitory.management.entity.Role;
import com.dormitory.management.entity.Student;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.PasswordResetTokenRepository;
import com.dormitory.management.repository.RoleRepository;
import com.dormitory.management.repository.StudentRepository;
import com.dormitory.management.security.JwtService;
import com.dormitory.management.security.UserPrincipal;
import com.dormitory.management.service.AuthService;
import com.dormitory.management.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.jwt.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Override
    @Transactional
    public CurrentUserResponseDTO registerStudent(RegisterStudentRequestDTO request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String fullName = request.getFullName() == null ? null : request.getFullName().trim();
        String studentCode = request.getStudentCode() == null ? null : request.getStudentCode().trim();
        String cccd = request.getCccd() == null ? null : request.getCccd().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();
        String gender = request.getGender() == null ? null : request.getGender().trim();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(fullName) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Username, full name and password are required");
        }

        if (!StringUtils.hasText(studentCode) || !StringUtils.hasText(cccd)) {
            throw new IllegalArgumentException("Student code and CCCD are required");
        }

        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (StringUtils.hasText(email) && appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (studentRepository.existsByStudentCode(studentCode)) {
            throw new IllegalArgumentException("Student code already exists");
        }

        if (studentRepository.existsByCccd(cccd)) {
            throw new IllegalArgumentException("CCCD already exists");
        }

        if (StringUtils.hasText(gender) && !isSupportedGender(gender)) {
            throw new IllegalArgumentException("Gender must be Nam or Nữ");
        }

        Role studentRole = roleRepository.findByRoleName("ROLE_STUDENT")
                .orElseThrow(() -> new IllegalArgumentException("ROLE_STUDENT is not configured"));

        Student student = Student.builder()
            .studentCode(studentCode)
            .fullName(fullName)
            .dateOfBirth(request.getDateOfBirth())
            .gender(StringUtils.hasText(gender) ? gender : null)
            .phone(StringUtils.hasText(phone) ? phone : null)
            .cccd(cccd)
            .email(StringUtils.hasText(email) ? email : null)
            .build();

        Student savedStudent = studentRepository.save(student);

        AppUser newUser = AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(fullName)
                .email(StringUtils.hasText(email) ? email : null)
                .enabled(true)
                .roles(java.util.Set.of(studentRole))
            .student(savedStudent)
                .build();

        AppUser saved = appUserRepository.save(newUser);
        return toCurrentUserDto(UserPrincipal.fromEntity(saved));
    }

    private boolean isSupportedGender(String gender) {
        return "Nam".equalsIgnoreCase(gender) || "Nữ".equalsIgnoreCase(gender);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        String loginInput = request.getUsername() == null ? null : request.getUsername().trim();

        if (!StringUtils.hasText(loginInput) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Username, email, or student code and password are required");
        }

        // Try to find user by username, email, or student code
        AppUser appUser = appUserRepository.findByUsername(loginInput)
                .or(() -> appUserRepository.findByEmail(loginInput.toLowerCase()))
                .or(() -> appUserRepository.findByStudentCode(loginInput))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username, email, student code or password"));

        // Authenticate using the actual username
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(appUser.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new IllegalArgumentException("Invalid username, email, student code or password");
        }

        UserPrincipal userPrincipal = UserPrincipal.fromEntity(appUser);
        String token = jwtService.generateToken(userPrincipal);
        CurrentUserResponseDTO currentUser = toCurrentUserDto(userPrincipal);

        return LoginResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpirationMs())
                .user(currentUser)
                .build();
    }

    @Override
    public CurrentUserResponseDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        return toCurrentUserDto(userPrincipal);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();

        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản với email này không tồn tại"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenExpiryHours);

        // Clear previous tokens for this user
        passwordResetTokenRepository.deleteByAppUser_Id(appUser.getId());

        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .appUser(appUser)
                .expiryDate(expiryDate)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        passwordResetTokenRepository.save(token);

        // Send email with reset link
        if (emailService != null) {
            String resetLink = frontendUrl + "/reset-password.html?token=" + resetToken;
            emailService.sendResetPasswordEmail(appUser.getEmail() != null ? appUser.getEmail() : email, resetLink);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        String token = request.getToken() == null ? null : request.getToken().trim();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token is required");
        }

        if (!StringUtils.hasText(newPassword) || !StringUtils.hasText(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password are required");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Liên kết đặt lại mật khẩu không hợp lệ"));

        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("Liên kết đã hết hạn hoặc đã được sử dụng");
        }

        AppUser appUser = resetToken.getAppUser();
        appUser.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(appUser);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private CurrentUserResponseDTO toCurrentUserDto(UserPrincipal userPrincipal) {
        List<String> roles = userPrincipal.getAuthorities()
                .stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .sorted()
                .toList();

        return CurrentUserResponseDTO.builder()
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .fullName(userPrincipal.getFullName())
                .email(userPrincipal.getEmail())
                .roles(roles)
                .build();
    }
}
