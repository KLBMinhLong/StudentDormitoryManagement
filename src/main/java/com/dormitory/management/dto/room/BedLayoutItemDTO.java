package com.dormitory.management.dto.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedLayoutItemDTO {

    private Long id;

    @NotNull(message = "Vị trí giường không được để trống")
    @Min(value = 1, message = "Vị trí giường phải từ 1 đến 8")
    @Max(value = 8, message = "Vị trí giường phải từ 1 đến 8")
    private Integer bedNumber;
}