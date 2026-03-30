package com.dormitory.management.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.student.ChangePasswordRequestDTO;
import com.dormitory.management.dto.student.StudentListItemDTO;
import com.dormitory.management.dto.student.StudentDTO;
import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Role;
import com.dormitory.management.entity.Student;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.RoleRepository;
import com.dormitory.management.repository.StudentRepository;
import com.dormitory.management.service.student.StudentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PagedResponseDTO<StudentListItemDTO> searchStudents(String keyword, int page, int size, String sortBy, String direction) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        String sortField = StringUtils.hasText(sortBy) ? sortBy : "id";

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<Student> studentPage;

        if (!StringUtils.hasText(keyword)) {
            studentPage = studentRepository.findAll(pageable);
        } else {
            studentPage = studentRepository.searchByCodeOrName(keyword.trim(), pageable);
        }

        Page<StudentListItemDTO> mappedPage = studentPage.map(this::mapToListItemDTO);
        return PagedResponseDTO.fromPage(mappedPage);
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        return mapToDTO(student);
    }

    @Override
    @Transactional
    public StudentDTO createStudent(StudentDTO dto) {
        if (studentRepository.existsByStudentCode(dto.getStudentCode())) {
            throw new RuntimeException("Student code already exists");
        }
        if (studentRepository.existsByCccd(dto.getCccd())) {
            throw new RuntimeException("CCCD already exists");
        }

        validateGender(dto.getGender());

        Student student = mapToEntity(dto);
        Student savedStudent = studentRepository.save(student);

        Role studentRole = roleRepository.findByRoleName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("Role STUDENT not found"));

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

        return mapToDTO(savedStudent);
    }

    @Override
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getStudentCode().equals(dto.getStudentCode()) && studentRepository.existsByStudentCodeAndIdNot(dto.getStudentCode(), id)) {
            throw new RuntimeException("Student code already exists");
        }
        if (!student.getCccd().equals(dto.getCccd()) && studentRepository.existsByCccdAndIdNot(dto.getCccd(), id)) {
            throw new RuntimeException("CCCD already exists");
        }

        validateGender(dto.getGender());

        student.setStudentCode(dto.getStudentCode());
        student.setFullName(dto.getFullName());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setPhone(dto.getPhone());
        student.setCccd(dto.getCccd());
        student.setEmail(dto.getEmail());

        Student updatedStudent = studentRepository.save(student);

        appUserRepository.findByStudentId(id).ifPresent((account) -> {
            account.setFullName(updatedStudent.getFullName());
            account.setEmail(updatedStudent.getEmail());
            account.setUsername(updatedStudent.getStudentCode());
            appUserRepository.save(account);
        });

        return mapToDTO(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found"));

        appUserRepository.findByStudentId(student.getId())
            .ifPresent(appUserRepository::delete);

        studentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public String uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return saveAvatarFile(student, file);
    }

    @Override
    @Transactional
    public StudentDTO getCurrentStudentProfile(String username) {
        return mapToDTO(resolveStudentByUsername(username));
    }

    @Override
    @Transactional
    public StudentDTO updateCurrentStudentProfile(String username, StudentDTO dto) {
        Student student = resolveStudentByUsername(username);

        validateGender(dto.getGender());

        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setPhone(dto.getPhone());
        student.setEmail(dto.getEmail());

        return mapToDTO(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequestDTO request) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), appUser.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        appUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(appUser);
    }

    @Override
    @Transactional
    public String uploadMyAvatar(String username, MultipartFile file) throws IOException {
        Student student = resolveStudentByUsername(username);
        return saveAvatarFile(student, file);
    }

    private Student resolveStudentByUsername(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (appUser.getStudent() != null) {
            return appUser.getStudent();
        }

        Student linked = studentRepository.findByStudentCodeIgnoreCase(appUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản chưa được liên kết hồ sơ sinh viên"));

        appUser.setStudent(linked);
        appUserRepository.save(appUser);
        return linked;
    }

    private String saveAvatarFile(Student student, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID() + extension;

        String uploadDir = "uploads/images/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        String avatarUrl = "/images/" + newFilename;
        student.setAvatarUrl(avatarUrl);
        studentRepository.save(student);

        return avatarUrl;
    }

    private StudentDTO mapToDTO(Student entity) {
        return StudentDTO.builder()
                .id(entity.getId())
                .studentCode(entity.getStudentCode())
                .fullName(entity.getFullName())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .phone(entity.getPhone())
                .cccd(entity.getCccd())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .build();
    }

    private StudentListItemDTO mapToListItemDTO(Student entity) {
        AppUser linkedUser = appUserRepository.findByStudentId(entity.getId()).orElse(null);
        return StudentListItemDTO.builder()
                .id(entity.getId())
                .studentCode(entity.getStudentCode())
                .fullName(entity.getFullName())
                .gender(entity.getGender())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .username(linkedUser == null ? null : linkedUser.getUsername())
                .hasAccount(linkedUser != null)
                .build();
    }

    private Student mapToEntity(StudentDTO dto) {
        Student student = new Student();
        student.setStudentCode(dto.getStudentCode());
        student.setFullName(dto.getFullName());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setPhone(dto.getPhone());
        student.setCccd(dto.getCccd());
        student.setEmail(dto.getEmail());
        return student;
    }

    private void validateGender(String gender) {
        if (!StringUtils.hasText(gender)) {
            return;
        }

        if (!"Nam".equalsIgnoreCase(gender) && !"Nữ".equalsIgnoreCase(gender)) {
            throw new RuntimeException("Giới tính chỉ được chọn Nam hoặc Nữ");
        }
    }
}