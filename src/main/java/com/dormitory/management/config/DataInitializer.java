package com.dormitory.management.config;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Role;
import com.dormitory.management.entity.Bed;
import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.RoomType;
import com.dormitory.management.entity.enums.RoomStatus;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.repository.RoleRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.repository.RoomTypeRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BedRepository bedRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeAuthData() {
        return args -> {
            initializeBuildingData();
            initializeRoomTypeData();
            initializeRoomData();
            initializeBedData();

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

    private void initializeRoomData() {
        Building buildingA = buildingRepository.findByNameIgnoreCase("Tòa A")
                .orElseThrow(() -> new IllegalStateException("Building Tòa A not found"));
        Building buildingB = buildingRepository.findByNameIgnoreCase("Tòa B")
                .orElseThrow(() -> new IllegalStateException("Building Tòa B not found"));
        Building buildingC = buildingRepository.findByNameIgnoreCase("Tòa C")
            .orElseThrow(() -> new IllegalStateException("Building Tòa C not found"));
        Building buildingD = buildingRepository.findByNameIgnoreCase("Tòa D")
            .orElseThrow(() -> new IllegalStateException("Building Tòa D not found"));

        RoomType roomType8 = roomTypeRepository.findByNameIgnoreCase("Phòng 8 người")
            .orElseThrow(() -> new IllegalStateException("Room type Phòng 8 người not found"));
        RoomType roomType4 = roomTypeRepository.findByNameIgnoreCase("Phòng 4 người")
            .orElseThrow(() -> new IllegalStateException("Room type Phòng 4 người not found"));
        RoomType roomTypeVip = roomTypeRepository.findByNameIgnoreCase("Phòng VIP")
            .orElseThrow(() -> new IllegalStateException("Room type Phòng VIP not found"));

        seedRoomsForBuilding(buildingA, "A", roomType8, roomType4, roomTypeVip);
        seedRoomsForBuilding(buildingB, "B", roomType8, roomType4, roomTypeVip);
        seedRoomsForBuilding(buildingC, "C", roomType8, roomType4, roomTypeVip);
        seedRoomsForBuilding(buildingD, "D", roomType8, roomType4, roomTypeVip);
    }

        private void seedRoomsForBuilding(
            Building building,
            String buildingCode,
            RoomType roomType8,
            RoomType roomType4,
            RoomType roomTypeVip) {
        for (int floor = 1; floor <= building.getTotalFloors(); floor++) {
            for (int roomNumber = 1; roomNumber <= 16; roomNumber++) {
                String code = String.format("%s%d%02d", buildingCode, floor, roomNumber);

                if (roomRepository.existsByBuildingIdAndRoomNumberIgnoreCase(building.getId(), code)) {
                    continue;
                }

                RoomType roomType = resolveRoomTypeByRoomIndex(roomNumber, roomType8, roomType4, roomTypeVip);

                Room room = Room.builder()
                        .roomNumber(code)
                        .status(RoomStatus.AVAILABLE)
                        .building(building)
                        .roomType(roomType)
                        .build();
                roomRepository.save(room);
            }
        }
    }

    private RoomType resolveRoomTypeByRoomIndex(
            int roomIndex,
            RoomType roomType8,
            RoomType roomType4,
            RoomType roomTypeVip) {
        if (roomIndex <= 8) {
            return roomType8;
        }
        if (roomIndex <= 14) {
            return roomType4;
        }
        return roomTypeVip;
    }

    private void initializeRoomTypeData() {
        upsertRoomType("Phòng 4 người", 4, new BigDecimal("1200000"), "Nam/Nữ");
        upsertRoomType("Phòng 8 người", 8, new BigDecimal("850000"), "Nam/Nữ");
        upsertRoomType("Phòng VIP", 2, new BigDecimal("2500000"), "Nam/Nữ");
    }

    private void initializeBedData() {
        for (Room room : roomRepository.findAll()) {
            int capacity = resolveRoomCapacity(room);
            var existingBeds = bedRepository.findByRoomIdOrderByBedNumberAsc(room.getId());

            for (int bedNumber = 1; bedNumber <= capacity; bedNumber++) {
                int currentNumber = bedNumber;
                boolean exists = existingBeds.stream().anyMatch((bed) -> bed.getBedNumber() == currentNumber);
                if (exists) {
                    continue;
                }

                Bed newBed = Bed.builder()
                        .bedNumber(bedNumber)
                        .isOccupied(false)
                        .room(room)
                        .student(null)
                        .build();
                bedRepository.save(newBed);
            }

            for (Bed bed : existingBeds) {
                if (bed.getBedNumber() <= capacity) {
                    continue;
                }
                if (bed.isOccupied() || bed.getStudent() != null) {
                    continue;
                }
                bedRepository.delete(bed);
            }
        }
    }

    private int resolveRoomCapacity(Room room) {
        if (room.getRoomType() == null || room.getRoomType().getId() == null) {
            return 0;
        }

        return roomTypeRepository.findById(room.getRoomType().getId())
                .map(RoomType::getCapacity)
                .map((capacity) -> Math.min(capacity, 8))
                .orElse(0);
    }

    private void upsertRoomType(String name, int capacity, BigDecimal basePrice, String genderAllowed) {
        RoomType roomType = roomTypeRepository.findByNameIgnoreCase(name)
                .orElseGet(RoomType::new);

        roomType.setName(name);
        roomType.setCapacity(capacity);
        roomType.setBasePrice(basePrice);
        roomType.setGenderAllowed(genderAllowed);
        roomTypeRepository.save(roomType);
    }

    private void initializeBuildingData() {
        upsertBuilding("Tòa A", "Toa A", 5, "Khu tòa dành cho sinh viên nam", "Nam");
        upsertBuilding("Tòa B", "Toa B", 5, "Khu tòa dành cho sinh viên nữ", "Nữ");

        // [Tòa dùng chung]: Phân loại là Nam/Nữ để cả 2 giới đều thấy, nhưng không dùng để ở (Ví dụ: Khu học tập)
        upsertBuilding("Tòa C", "Toa C", 7, "Khu tòa phòng học tập và sinh hoạt", "Nam/Nữ");

        upsertBuilding("Tòa D", "Toa D", 9, "Khu tòa mở rộng cho sinh viên mới", "Nam/Nữ");
    }

    private void upsertBuilding(String canonicalName, String legacyName, int totalFloors, String description, String genderAllowed) {
        Building building = buildingRepository.findByNameIgnoreCase(canonicalName)
                .or(() -> buildingRepository.findByNameIgnoreCase(legacyName))
                .orElseGet(Building::new);

        building.setName(canonicalName);
        building.setTotalFloors(totalFloors);
        building.setDescription(description);
        building.setGenderAllowed(genderAllowed);
        buildingRepository.save(building);
    }

    private Role findOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));
    }
}
