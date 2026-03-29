package com.dormitory.management.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.room.RoomDTO;
import com.dormitory.management.entity.Room;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.service.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::toRoomDto)
                .toList();
    }

    private RoomDTO toRoomDto(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus() == null ? null : room.getStatus().name())
                .buildingName(room.getBuilding() == null ? null : room.getBuilding().getName())
                .roomTypeName(room.getRoomType() == null ? null : room.getRoomType().getName())
                .build();
    }
}
