package com.dormitory.management.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    private Long id;
    private String roomNumber;
    private String status;
    private Long buildingId;
    private String buildingName;
    private Long roomTypeId;
    private String roomTypeName;
    private Integer totalBeds;
    private Integer occupiedBeds;
    private List<BedDTO> beds;
}
