package com.dormitory.management.dto.issue;

import com.dormitory.management.entity.enums.IssuePriority;
import com.dormitory.management.entity.enums.IssueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponseDTO {
    private Long id;
    private String description;
    private IssuePriority priority;
    private IssueStatus status;
    private LocalDateTime createdAt;
    private String studentName;
    private String studentCode;
    private String roomNumber;
    private String buildingName;
}
