package com.dormitory.management.service.impl;

import com.dormitory.management.dto.room.BedDTO;
import com.dormitory.management.entity.Bed;
import com.dormitory.management.entity.Room;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.service.BedService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BedDTO> getBedsByRoomId(Long roomId) {
        return bedRepository.findByRoomIdOrderByBedNumberAsc(roomId)
                .stream()
                .map(this::toBedDto)
                .toList();
    }

    @Override
    public BedDTO createBed(Long roomId, BedDTO bedDTO) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Bed bed = modelMapper.map(bedDTO, Bed.class);
        bed.setRoom(room);
        bed = bedRepository.save(bed);
        return toBedDto(bed);
    }

    @Override
    public BedDTO updateBed(Long id, BedDTO bedDTO) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bed not found"));
        modelMapper.map(bedDTO, bed);
        bed = bedRepository.save(bed);
        return toBedDto(bed);
    }

    @Override
    public void deleteBed(Long id) {
        bedRepository.deleteById(id);
    }

    @Override
    public void deleteBedsByRoom(Long roomId) {
        bedRepository.deleteByRoomId(roomId);
    }

    private BedDTO toBedDto(Bed bed) {
        LocalDateTime now = LocalDateTime.now();
        boolean reserved = bed.getStudent() == null
                && bed.getReservedUntil() != null
                && bed.getReservedUntil().isAfter(now);
        boolean occupied = bed.getStudent() != null || (bed.isOccupied() && !reserved);

        String occupancyStatus = reserved
            ? "RESERVED"
            : (occupied ? "OCCUPIED" : "AVAILABLE");

        return BedDTO.builder()
                .id(bed.getId())
                .bedNumber(bed.getBedNumber())
                .isOccupied(occupied)
                .studentName(bed.getStudent() != null ? bed.getStudent().getFullName() : null) // Assume Student has getFullName()
            .occupancyStatus(occupancyStatus)
            .reservedUntil(bed.getReservedUntil())
                .build();
    }
}

