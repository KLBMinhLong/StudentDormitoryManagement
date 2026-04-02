package com.dormitory.management.service;

import com.dormitory.management.dto.issue.IssueRequestDTO;
import com.dormitory.management.dto.issue.IssueResponseDTO;
import com.dormitory.management.dto.issue.IssueStatusUpdateDTO;

import java.util.List;

public interface IssueService {
    IssueResponseDTO createIssue(String username, IssueRequestDTO request);
    List<IssueResponseDTO> getMyIssues(String username);
    List<IssueResponseDTO> getAllIssues();
    IssueResponseDTO updateIssueStatus(Long id, IssueStatusUpdateDTO request);
}
