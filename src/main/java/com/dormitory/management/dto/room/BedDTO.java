package com.dormitory.management.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedDTO {
    private Long id;
    private int bedNumber;
    private boolean isOccupied;
    private String studentName; // Optional, null if free
}

