package com.dormitory.management.dto.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityRecordPrefillDTO {

    private Long roomId;
    private Integer month;
    private Integer year;

    private boolean hasPreviousRecord;
    private Integer previousMonth;
    private Integer previousYear;

    private Double suggestedOldElectric;
    private Double suggestedOldWater;

    private Double previousElectricConsumption;
    private Double previousWaterConsumption;
}
