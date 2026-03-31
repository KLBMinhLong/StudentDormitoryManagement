package com.dormitory.management.dto.utility;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityRecordBatchRequestDTO {

    private Long buildingId;

    @NotEmpty(message = "Danh sách chỉ số điện nước không được trống")
    @Valid
    private List<UtilityRecordRequestDTO> records;
}
