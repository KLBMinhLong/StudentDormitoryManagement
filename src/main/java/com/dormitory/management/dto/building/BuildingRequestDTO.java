package com.dormitory.management.dto.building;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class BuildingRequestDTO {

    @NotBlank(message = "Building name must not be blank")
    @Size(max = 150, message = "Building name must be at most 150 characters")
    private String name;

    @NotNull(message = "Total floors must not be null")
    @Positive(message = "Total floors must be greater than 0")
    private Integer totalFloors;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
}
