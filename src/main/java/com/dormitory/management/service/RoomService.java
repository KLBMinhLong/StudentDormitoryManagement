package com.dormitory.management.service;

import java.util.List;

import com.dormitory.management.dto.room.BedDTO;
import com.dormitory.management.dto.room.RoomDTO;

public interface RoomService {

    List<RoomDTO> getAllRooms();
    
    RoomDTO getRoomById(Long id);
    
    List<BedDTO> getBedsByRoomId(Long roomId);
}
