package com.dormitory.management.dto.room;

import com.dormitory.management.entity.enums.RoomStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RoomRequestDTO {

    @NotBlank(message = "Room number must not be blank")
    @Size(max = 20, message = "Room number must be at most 20 characters")
    private String roomNumber;

    @NotNull(message = "Building id must not be null")
    private Long buildingId;

    @NotNull(message = "Room type id must not be null")
    private Long roomTypeId;

    private RoomStatus status;
}
