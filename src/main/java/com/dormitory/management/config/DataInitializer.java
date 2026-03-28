package com.dormitory.management.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Role;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeAuthData() {
        return args -> {
            Role adminRole = findOrCreateRole("ROLE_ADMIN");
            Role studentRole = findOrCreateRole("ROLE_STUDENT");

            if (!appUserRepository.existsByUsername("admin")) {
                AppUser adminUser = AppUser.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .fullName("System Administrator")
                        .email("admin@dormitory.local")
                        .enabled(true)
                        .roles(Set.of(adminRole))
                        .build();
                appUserRepository.save(adminUser);
            }

            if (!appUserRepository.existsByUsername("student")) {
                AppUser studentUser = AppUser.builder()
                        .username("student")
                        .password(passwordEncoder.encode("student123"))
                        .fullName("Default Student")
                        .email("student@dormitory.local")
                        .enabled(true)
                        .roles(Set.of(studentRole))
                        .build();
                appUserRepository.save(studentUser);
            }
        };
    }

    private Role findOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));
    }
}
