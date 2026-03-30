package com.dormitory.management.dto.room;

import java.time.LocalDateTime;

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
    private String occupancyStatus;
    private LocalDateTime reservedUntil;
}

