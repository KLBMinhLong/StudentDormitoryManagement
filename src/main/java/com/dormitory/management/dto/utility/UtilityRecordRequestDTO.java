package com.dormitory.management.dto.utility;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityRecordRequestDTO {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be valid")
    private Integer year;

    @NotNull(message = "Old electric index is required")
    @PositiveOrZero(message = "Old electric index must be zero or positive")
    private Double oldElectric;

    @NotNull(message = "New electric index is required")
    @PositiveOrZero(message = "New electric index must be zero or positive")
    private Double newElectric;

    @NotNull(message = "Old water index is required")
    @PositiveOrZero(message = "Old water index must be zero or positive")
    private Double oldWater;

    @NotNull(message = "New water index is required")
    @PositiveOrZero(message = "New water index must be zero or positive")
    private Double newWater;
}
