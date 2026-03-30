package com.dormitory.management.dto.contract;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequestDTO {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Bed ID is required")
    private Long bedId;

    @NotNull(message = "Duration months is required")
    private Integer durationMonths;
}