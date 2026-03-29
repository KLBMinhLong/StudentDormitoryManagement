package com.dormitory.management.dto.room;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedLayoutRequestDTO {

    @Valid
    @Size(max = 8, message = "Mỗi phòng chỉ được tối đa 8 giường")
    private List<BedLayoutItemDTO> beds;
}