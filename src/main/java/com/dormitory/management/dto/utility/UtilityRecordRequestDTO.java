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

    @NotNull(message = "Mã phòng không được để trống")
    private Long roomId;

    @NotNull(message = "Tháng không được để trống")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12")
    private Integer month;

    @NotNull(message = "Năm không được để trống")
    @Min(value = 2000, message = "Năm phải hợp lệ")
    private Integer year;

    @NotNull(message = "Chỉ số điện cũ không được để trống")
    @PositiveOrZero(message = "Chỉ số điện cũ phải >= 0")
    private Double oldElectric;

    @NotNull(message = "Chỉ số điện mới không được để trống")
    @PositiveOrZero(message = "Chỉ số điện mới phải >= 0")
    private Double newElectric;

    @NotNull(message = "Chỉ số nước cũ không được để trống")
    @PositiveOrZero(message = "Chỉ số nước cũ phải >= 0")
    private Double oldWater;

    @NotNull(message = "Chỉ số nước mới không được để trống")
    @PositiveOrZero(message = "Chỉ số nước mới phải >= 0")
    private Double newWater;
}
