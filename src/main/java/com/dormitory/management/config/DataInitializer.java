package com.dormitory.management.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Role;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final BuildingRepository buildingRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeAuthData() {
        return args -> {
            initializeBuildingData();

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

    private void initializeBuildingData() {
        upsertBuilding("Tòa A", "Toa A", 5, "Khu tòa dành cho sinh viên nam");
        upsertBuilding("Tòa B", "Toa B", 5, "Khu tòa dành cho sinh viên nữ");
        upsertBuilding("Tòa C", "Toa C", 7, "Khu tòa phòng học tập và sinh hoạt");
        upsertBuilding("Tòa D", "Toa D", 9, "Khu tòa mở rộng cho sinh viên mới");
    }

    private void upsertBuilding(String canonicalName, String legacyName, int totalFloors, String description) {
        Building building = buildingRepository.findByNameIgnoreCase(canonicalName)
                .or(() -> buildingRepository.findByNameIgnoreCase(legacyName))
                .orElseGet(Building::new);

        building.setName(canonicalName);
        building.setTotalFloors(totalFloors);
        building.setDescription(description);
        buildingRepository.save(building);
    }

    private Role findOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));
    }
}
