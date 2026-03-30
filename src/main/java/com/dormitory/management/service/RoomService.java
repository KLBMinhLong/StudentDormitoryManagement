package com.dormitory.management.service;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.room.BedDTO;
import com.dormitory.management.dto.room.BedLayoutRequestDTO;
import com.dormitory.management.dto.room.BedOccupancyRequestDTO;
import com.dormitory.management.dto.room.RoomDTO;
import com.dormitory.management.dto.room.RoomRequestDTO;
import com.dormitory.management.entity.enums.RoomStatus;

public interface RoomService {

        PagedResponseDTO<RoomDTO> getAllRooms(
                String keyword,
                        String genderAllowed,
            Long buildingId,
            RoomStatus status,
            int page,
            int size,
            String sortBy,
            String direction);

    RoomDTO getRoomById(Long id);

    RoomDTO createRoom(RoomRequestDTO request);

    RoomDTO updateRoom(Long id, RoomRequestDTO request);

    void deleteRoom(Long id);

    PagedResponseDTO<BedDTO> getBedsByRoomId(Long roomId, int page, int size, String sortBy, String direction);

        BedDTO updateBedOccupancy(Long roomId, Long bedId, BedOccupancyRequestDTO request);

        RoomDTO saveBedLayout(Long roomId, BedLayoutRequestDTO request);
}
