package com.dormitory.management.dto.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityConsumptionTimelineItemDTO {

    private Integer month;
    private Integer year;
    private String periodLabel;
    private Double totalElectricConsumption;
    private Double totalWaterConsumption;
    private Long recordCount;
}
