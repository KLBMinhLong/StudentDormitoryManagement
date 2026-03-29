package com.dormitory.management.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    private Long id;
    private String roomNumber;
    private String status;
    private String buildingName;
    private String roomTypeName;
}
