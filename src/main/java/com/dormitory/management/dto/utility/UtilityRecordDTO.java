package com.dormitory.management.dto.utility;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityRecordDTO {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private Long buildingId;
    private String buildingName;
    private Integer month;
    private Integer year;
    private Double oldElectric;
    private Double newElectric;
    private Double electricConsumption;
    private Double oldWater;
    private Double newWater;
    private Double waterConsumption;
    private LocalDateTime createdAt;
}
