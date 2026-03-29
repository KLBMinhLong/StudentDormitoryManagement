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
public class RoomWithBedsDTO {
    private Long id;
    private String roomNumber;
    private String status;
    private String buildingName;
    private String roomTypeName;
    private List<BedDTO> beds;
}

