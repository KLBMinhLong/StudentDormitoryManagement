package com.dormitory.management.service;

import com.dormitory.management.dto.room.BedDTO;
import java.util.List;

public interface BedService {
    List<BedDTO> getBedsByRoomId(Long roomId);
    BedDTO createBed(Long roomId, BedDTO bedDTO);
    BedDTO updateBed(Long id, BedDTO bedDTO);
    void deleteBed(Long id);
    void deleteBedsByRoom(Long roomId);
}

