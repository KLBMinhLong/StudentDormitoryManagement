package com.dormitory.management.dto.building;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDTO {

    private Long id;
    private String name;
    private int totalFloors;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
