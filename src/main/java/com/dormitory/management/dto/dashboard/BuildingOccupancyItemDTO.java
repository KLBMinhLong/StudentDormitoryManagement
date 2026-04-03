package com.dormitory.management.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingOccupancyItemDTO {
    private Long buildingId;
    private String buildingName;
    private long totalBeds;
    private long occupiedBeds;
    private double occupancyRate;
}
