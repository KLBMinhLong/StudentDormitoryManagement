package com.dormitory.management.dto.invoice;

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
public class InvoiceGenerateRequestDTO {

    private Long buildingId;

    @NotNull(message = "Tháng không được để trống")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12")
    private Integer month;

    @NotNull(message = "Năm không được để trống")
    @Min(value = 2000, message = "Năm phải hợp lệ")
    private Integer year;
}
