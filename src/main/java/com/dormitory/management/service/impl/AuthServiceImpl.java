package com.dormitory.management.service.impl;

import java.util.List;

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
import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Role;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.RoleRepository;
import com.dormitory.management.security.JwtService;
import com.dormitory.management.security.UserPrincipal;
import com.dormitory.management.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public CurrentUserResponseDTO registerStudent(RegisterStudentRequestDTO request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String fullName = request.getFullName() == null ? null : request.getFullName().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(fullName) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Username, full name and password are required");
        }

        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (StringUtils.hasText(email) && appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role studentRole = roleRepository.findByRoleName("ROLE_STUDENT")
                .orElseThrow(() -> new IllegalArgumentException("ROLE_STUDENT is not configured"));

        AppUser newUser = AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(fullName)
                .email(StringUtils.hasText(email) ? email : null)
                .enabled(true)
                .roles(java.util.Set.of(studentRole))
                .build();

        AppUser saved = appUserRepository.save(newUser);
        return toCurrentUserDto(UserPrincipal.fromEntity(saved));
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Username and password are required");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

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
