package com.dormitory.management.dto.room;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedOccupancyRequestDTO {

    @NotNull(message = "Trạng thái sử dụng giường không được để trống")
    private Boolean occupied;
}