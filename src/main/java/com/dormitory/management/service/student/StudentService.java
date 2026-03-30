package com.dormitory.management.service.student;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.student.ChangePasswordRequestDTO;
import com.dormitory.management.dto.student.StudentListItemDTO;
import com.dormitory.management.dto.student.StudentResidenceHistoryItemDTO;
import com.dormitory.management.dto.student.StudentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StudentService {
    // Do admin qản lý
    PagedResponseDTO<StudentListItemDTO> searchStudents(String keyword, int page, int size, String sortBy, String direction);
    StudentDTO getStudentById(Long id);
    StudentDTO createStudent(StudentDTO dto);
    StudentDTO updateStudent(Long id, StudentDTO dto);
    void deleteStudent(Long id);

    String uploadAvatar(Long studentId, MultipartFile file) throws IOException;

    // Do student tự quản lý
    StudentDTO getCurrentStudentProfile(String username);
    StudentDTO updateCurrentStudentProfile(String username, StudentDTO dto);
    String uploadMyAvatar(String username, MultipartFile file) throws IOException;
    void changePassword(String username, ChangePasswordRequestDTO request);
        PagedResponseDTO<StudentResidenceHistoryItemDTO> getStudentResidenceHistory(
            Long studentId,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction);
        PagedResponseDTO<StudentResidenceHistoryItemDTO> getCurrentStudentResidenceHistory(
            String username,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction);
}