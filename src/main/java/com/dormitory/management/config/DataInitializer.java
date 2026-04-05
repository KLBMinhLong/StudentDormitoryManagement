package com.dormitory.management.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Role;
import com.dormitory.management.entity.Bed;
import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.RoomType;
import com.dormitory.management.entity.Student;
import com.dormitory.management.entity.enums.RoomStatus;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.repository.RoleRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.repository.RoomTypeRepository;
import com.dormitory.management.repository.StudentRepository;

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
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
    public CommandLineRunner initializeAuthData() {
        return args -> {
            if (isSeedDataAlreadyPresent()) {
                return;
            }

            // Initialize roles
            Role adminRole = findOrCreateRole("ROLE_ADMIN");
            Role studentRole = findOrCreateRole("ROLE_STUDENT");

            // Initialize system users
            initializeSystemUsers(adminRole, studentRole);

            // Initialize building data
            initializeBuildingData();
            initializeRoomTypeData();
            initializeRoomData();
            initializeBedData();

            // Initialize mock students
            initializeMockStudents(studentRole);
        };
    }

    private boolean isSeedDataAlreadyPresent() {
        return roleRepository.findByRoleName("ROLE_ADMIN").isPresent()
            && roleRepository.findByRoleName("ROLE_STUDENT").isPresent()
                && buildingRepository.count() > 0
                && roomTypeRepository.count() > 0
                && roomRepository.count() > 0
                && bedRepository.count() > 0
                && studentRepository.count() > 0;
    }

    private void initializeSystemUsers(Role adminRole, Role studentRole) {
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
    }

    private void initializeMockStudents(Role studentRole) {
        if (studentRepository.count() == 0) {
            List<Student> mockStudents = List.of(
                    createMockStudent("SV001", "Bùi Bình Nguyên", "2004-05-15", "Nam", "0987654321", "079004000001", "nguyen.bb@hutech.edu.vn", "/images/sv001.png"),
                    createMockStudent("SV002", "Nguyễn Minh Long", "2004-01-20", "Nam", "0912345678", "079004000002", "hai.nl@hutech.edu.vn", "/images/sv002.png"),
                    createMockStudent("SV003", "Đinh Thanh Dân", "2003-11-30", "Nữ", "0905111222", "079004000003", "tam.tm@hutech.edu.vn", "/images/sv003.png"),
                    createMockStudent("SV004", "Phan Nhật Duy", "2004-03-12", "Nam", "0934555666", "079004000004", "nam.lh@hutech.edu.vn", "/images/sv004.png"),
                    createMockStudent("SV005", "Trương Phi Ân", "2004-07-25", "Nữ", "0977888999", "079004000005", "thao.pt@hutech.edu.vn", "/images/sv005.png"),
                    createMockStudent("SV006", "Ngô Tuấn Anh", "2004-09-05", "Nam", "0981222333", "079004000006", "bao.hg@hutech.edu.vn", null),
                    createMockStudent("SV007", "Vũ Phương Anh", "2004-12-10", "Nữ", "0922333444", "079004000007", "anh.vp@hutech.edu.vn", null),
                    createMockStudent("SV008", "Đặng Quang Huy", "2003-05-18", "Nam", "0966777888", "079004000008", "huy.dq@hutech.edu.vn", null),
                    createMockStudent("SV009", "Ngô Quỳnh Chi", "2004-08-22", "Nữ", "0944555111", "079004000009", "chi.nq@hutech.edu.vn", null),
                    createMockStudent("SV010", "Đỗ Minh Đức", "2004-02-14", "Nam", "0955666222", "079004000010", "duc.dm@hutech.edu.vn", null)
            );

            for (Student student : mockStudents) {
                Student savedStudent = studentRepository.save(student);

                if (!appUserRepository.existsByUsername(savedStudent.getStudentCode())) {
                    AppUser newAccount = AppUser.builder()
                            .username(savedStudent.getStudentCode())
                            .password(passwordEncoder.encode(savedStudent.getStudentCode()))
                            .fullName(savedStudent.getFullName())
                            .email(savedStudent.getEmail())
                            .enabled(true)
                            .roles(Set.of(studentRole))
                            .student(savedStudent)
                            .build();

                    appUserRepository.save(newAccount);
                }
            }
        }
    }

    private Student createMockStudent(String code, String name, String dob, String gender, String phone, String cccd, String email, String avatarUrl) {
        return Student.builder()
                .studentCode(code)
                .fullName(name)
                .dateOfBirth(LocalDate.parse(dob))
                .gender(gender)
                .phone(phone)
                .cccd(cccd)
                .email(email)
                .avatarUrl(avatarUrl)
                .build();
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
                String roomGender = resolveRoomGenderByPolicy(building, floor);

                if (roomRepository.existsByBuildingIdAndRoomNumberIgnoreCase(building.getId(), code)) {
                    continue;
                }

                RoomType roomType = resolveRoomTypeByRoomIndex(roomNumber, roomType8, roomType4, roomTypeVip);

                Room room = Room.builder()
                        .roomNumber(code)
                        .status(RoomStatus.AVAILABLE)
                        .building(building)
                        .roomType(roomType)
                        .genderAllowed(roomGender)
                        .build();
                roomRepository.save(room);
            }
        }
    }

    private String resolveRoomGenderByPolicy(Building building, int floor) {
        if (building == null || building.getGenderAllowed() == null) {
            return "Nam/Nữ";
        }

        String buildingGender = building.getGenderAllowed().trim();
        if (!"Nam/Nữ".equalsIgnoreCase(buildingGender)) {
            return buildingGender;
        }

        // Tòa hỗn hợp: tầng lẻ cho Nữ, tầng chẵn cho Nam.
        return floor % 2 == 0 ? "Nam" : "Nữ";
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
        upsertRoomType("Phòng 8 người", 8, new BigDecimal("300000"), "Nam/Nữ");
        upsertRoomType("Phòng 4 người", 4, new BigDecimal("600000"), "Nam/Nữ");
        upsertRoomType("Phòng VIP", 2, new BigDecimal("1200000"), "Nam/Nữ");
    }

    private void initializeBedData() {
        for (Room room : roomRepository.findAll()) {
            int capacity = room.getRoomType() != null ? room.getRoomType().getCapacity() : 0;
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
                        .reservedUntil(null)
                        .reservedContractId(null)
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
        upsertBuilding("Tòa C", "Toa C", 7, "Khu tòa Mix nam nữ", "Nam/Nữ");
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
