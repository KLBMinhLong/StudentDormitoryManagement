package com.dormitory.management.dto.issue;

import com.dormitory.management.entity.enums.IssuePriority;
import com.dormitory.management.entity.enums.IssueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueStatusUpdateDTO {
    private IssueStatus status;
    private IssuePriority priority;
}
