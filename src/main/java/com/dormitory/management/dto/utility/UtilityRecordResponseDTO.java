package com.dormitory.management.dto.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityRecordResponseDTO {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private Long buildingId;
    private String buildingName;
    private Integer month;
    private Integer year;
    private Double oldElectric;
    private Double newElectric;
    private Double oldWater;
    private Double newWater;
    private String periodStatus;
    private Boolean hasInvoice;
}
