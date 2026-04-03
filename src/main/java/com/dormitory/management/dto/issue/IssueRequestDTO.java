package com.dormitory.management.dto.issue;

import com.dormitory.management.entity.enums.IssuePriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueRequestDTO {

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Độ ưu tiên không được để trống")
    private IssuePriority priority;
}
