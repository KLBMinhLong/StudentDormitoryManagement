package com.dormitory.management.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dormitory.management.dto.student.ChangePasswordRequestDTO;
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
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentRepository.searchByCodeOrName(keyword).stream().map(this::mapToDTO).collect(Collectors.toList());
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

        if (!student.getStudentCode().equals(dto.getStudentCode()) && studentRepository.existsByStudentCode(dto.getStudentCode())) {
            throw new RuntimeException("Student code already exists");
        }
        if (!student.getCccd().equals(dto.getCccd()) && studentRepository.existsByCccd(dto.getCccd())) {
            throw new RuntimeException("CCCD already exists");
        }

        student.setStudentCode(dto.getStudentCode());
        student.setFullName(dto.getFullName());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setPhone(dto.getPhone());
        student.setCccd(dto.getCccd());
        student.setEmail(dto.getEmail());

        return mapToDTO(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found");
        }

        Student student = studentRepository.findById(id).get();
        appUserRepository.findByUsername(student.getStudentCode())
                .ifPresent(appUser -> appUserRepository.delete(appUser));

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
    public StudentDTO getCurrentStudentProfile(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (appUser.getStudent() == null) {
            throw new RuntimeException("User is not linked to any student profile");
        }
        return mapToDTO(appUser.getStudent());
    }

    @Override
    @Transactional
    public StudentDTO updateCurrentStudentProfile(String username, StudentDTO dto) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = appUser.getStudent();
        if (student == null) {
            throw new RuntimeException("User is not linked to any student profile");
        }

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
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = appUser.getStudent();
        if (student == null) {
            throw new RuntimeException("User is not linked to any student profile");
        }

        return saveAvatarFile(student, file);
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
}