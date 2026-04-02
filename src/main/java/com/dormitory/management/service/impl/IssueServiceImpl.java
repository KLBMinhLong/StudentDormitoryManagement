package com.dormitory.management.service.impl;

import com.dormitory.management.dto.issue.IssueRequestDTO;
import com.dormitory.management.dto.issue.IssueResponseDTO;
import com.dormitory.management.dto.issue.IssueStatusUpdateDTO;
import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Contract;
import com.dormitory.management.entity.Issue;
import com.dormitory.management.entity.Student;
import com.dormitory.management.entity.enums.ContractStatus;
import com.dormitory.management.entity.enums.IssueStatus;
import com.dormitory.management.exception.AppException;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.ContractRepository;
import com.dormitory.management.repository.IssueRepository;
import com.dormitory.management.repository.StudentRepository;
import com.dormitory.management.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final StudentRepository studentRepository;
    private final ContractRepository contractRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public IssueResponseDTO createIssue(String username, IssueRequestDTO request) {
        Student student = resolveStudentByUsername(username);
        
        // Find active contract to get the room
        Contract activeContract = contractRepository.findFirstByStudentIdAndStatusOrderByCreatedAtDesc(
                student.getId(), ContractStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Bạn không có hợp đồng lưu trú hoạt động để gửi yêu cầu sửa chữa."));

        Issue issue = Issue.builder()
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(IssueStatus.PENDING)
                .student(student)
                .room(activeContract.getRoom())
                .build();

        return toResponseDTO(issueRepository.save(issue));
    }

    @Override
    public List<IssueResponseDTO> getMyIssues(String username) {
        return issueRepository.findByStudent_AppUser_Username(username)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IssueResponseDTO> getAllIssues() {
        return issueRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IssueResponseDTO updateIssueStatus(Long id, IssueStatusUpdateDTO request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy yêu cầu sửa chữa"));
        
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        
        return toResponseDTO(issueRepository.save(issue));
    }

    private Student resolveStudentByUsername(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản"));

        if (appUser.getStudent() != null) {
            return appUser.getStudent();
        }

        return studentRepository.findByStudentCodeIgnoreCase(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hồ sơ sinh viên"));
    }

    private IssueResponseDTO toResponseDTO(Issue issue) {
        return IssueResponseDTO.builder()
                .id(issue.getId())
                .description(issue.getDescription())
                .priority(issue.getPriority())
                .status(issue.getStatus())
                .createdAt(issue.getCreatedAt())
                .studentName(issue.getStudent().getFullName())
                .studentCode(issue.getStudent().getStudentCode())
                .roomNumber(issue.getRoom().getRoomNumber())
                .buildingName(issue.getRoom().getBuilding().getName())
                .build();
    }
}
