package com.dormitory.management.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.dormitory.management.dto.room.BedDTO;
import com.dormitory.management.dto.room.RoomDTO;
import com.dormitory.management.entity.Bed;
import com.dormitory.management.entity.Room;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.service.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    
    @Autowired
    private BedRepository bedRepository;

    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::toRoomDto)
                .toList();
    }

    @Override
    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        List<Bed> beds = bedRepository.findByRoomIdOrderByBedNumberAsc(id);
        RoomDTO dto = toRoomDto(room);
        dto.setBeds(beds.stream().map(this::toBedDto).toList());
        return dto;
    }

    @Override
    public List<BedDTO> getBedsByRoomId(Long roomId) {
        return bedRepository.findByRoomIdOrderByBedNumberAsc(roomId)
                .stream()
                .map(this::toBedDto)
                .toList();
    }

    private RoomDTO toRoomDto(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus() == null ? null : room.getStatus().name())
                .buildingName(room.getBuilding() == null ? null : room.getBuilding().getName())
                .roomTypeName(room.getRoomType() == null ? null : room.getRoomType().getName())
                .beds(null)
                .build();
    }

    private BedDTO toBedDto(Bed bed) {
        return BedDTO.builder()
                .id(bed.getId())
                .bedNumber(bed.getBedNumber())
                .isOccupied(bed.isOccupied())
                .studentName(bed.getStudent() != null ? bed.getStudent().getFullName() : null)
                .build();
    }
}
