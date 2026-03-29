package com.dormitory.management.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.room.BedDTO;
import com.dormitory.management.dto.room.RoomDTO;
import com.dormitory.management.dto.room.RoomRequestDTO;
import com.dormitory.management.entity.Bed;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.RoomType;
import com.dormitory.management.entity.enums.RoomStatus;
import com.dormitory.management.exception.ResourceNotFoundException;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.repository.RoomTypeRepository;
import com.dormitory.management.service.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final BuildingRepository buildingRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    public PagedResponseDTO<RoomDTO> getAllRooms(
            String keyword,
            Long buildingId,
            RoomStatus status,
            int page,
            int size,
            String sortBy,
            String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<RoomDTO> result = roomRepository.findByFilters(buildingId, status, normalizedKeyword, pageable).map(this::toRoomDto);
        return PagedResponseDTO.fromPage(result);
    }

    @Override
    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        List<Bed> beds = bedRepository.findByRoomIdOrderByBedNumberAsc(id);
        RoomDTO dto = toRoomDto(room);
        dto.setBeds(beds.stream().map(this::toBedDto).toList());
        return dto;
    }

    @Override
    @Transactional
    public RoomDTO createRoom(RoomRequestDTO request) {
        Building building = findBuildingOrThrow(request.getBuildingId());
        RoomType roomType = findRoomTypeOrThrow(request.getRoomTypeId());
        String normalizedRoomNumber = normalizeRoomNumber(request.getRoomNumber());

        if (roomRepository.existsByBuildingIdAndRoomNumberIgnoreCase(building.getId(), normalizedRoomNumber)) {
            throw new IllegalArgumentException("Room number already exists in selected building");
        }

        Room room = Room.builder()
                .roomNumber(normalizedRoomNumber)
                .building(building)
                .roomType(roomType)
                .status(request.getStatus() == null ? RoomStatus.AVAILABLE : request.getStatus())
                .build();

        Room saved = roomRepository.save(room);
        syncBedsForRoom(saved, roomType.getCapacity());
        return toRoomDto(saved);
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(Long id, RoomRequestDTO request) {
        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        Building building = findBuildingOrThrow(request.getBuildingId());
        RoomType roomType = findRoomTypeOrThrow(request.getRoomTypeId());
        String normalizedRoomNumber = normalizeRoomNumber(request.getRoomNumber());

        if (roomRepository.existsByBuildingIdAndRoomNumberIgnoreCaseAndIdNot(building.getId(), normalizedRoomNumber, id)) {
            throw new IllegalArgumentException("Room number already exists in selected building");
        }

        existing.setRoomNumber(normalizedRoomNumber);
        existing.setBuilding(building);
        existing.setRoomType(roomType);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        Room updated = roomRepository.save(existing);
        syncBedsForRoom(updated, roomType.getCapacity());
        return toRoomDto(updated);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        List<Bed> beds = bedRepository.findByRoomIdOrderByBedNumberAsc(id);
        boolean hasOccupiedBed = beds.stream().anyMatch(Bed::isOccupied);
        if (hasOccupiedBed) {
            throw new IllegalArgumentException("Cannot delete room while one or more beds are occupied");
        }

        bedRepository.deleteByRoomId(id);
        roomRepository.delete(existing);
    }

    @Override
        public PagedResponseDTO<BedDTO> getBedsByRoomId(Long roomId, int page, int size, String sortBy, String direction) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        List<BedDTO> beds = bedRepository.findByRoomIdOrderByBedNumberAsc(roomId)
                .stream()
                .map(this::toBedDto)
                .toList();

        Comparator<BedDTO> comparator = "id".equalsIgnoreCase(sortBy)
            ? Comparator.comparing(BedDTO::getId, Comparator.nullsLast(Long::compareTo))
            : Comparator.comparing(BedDTO::getBedNumber, Comparator.nullsLast(Integer::compareTo));

        if (!"asc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        List<BedDTO> sortedBeds = beds.stream().sorted(comparator).toList();
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min(safePage * safeSize, sortedBeds.size());
        int toIndex = Math.min(fromIndex + safeSize, sortedBeds.size());
        List<BedDTO> content = sortedBeds.subList(fromIndex, toIndex);

        int totalElements = sortedBeds.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        boolean last = totalPages == 0 || safePage >= totalPages - 1;

        return PagedResponseDTO.<BedDTO>builder()
            .content(content)
            .pageNo(safePage)
            .pageSize(safeSize)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .last(last)
            .build();
    }

    private void syncBedsForRoom(Room room, int capacity) {
        List<Bed> existingBeds = new ArrayList<>(bedRepository.findByRoomIdOrderByBedNumberAsc(room.getId()));
        existingBeds.sort(Comparator.comparingInt(Bed::getBedNumber));

        if (existingBeds.size() < capacity) {
            for (int bedNumber = existingBeds.size() + 1; bedNumber <= capacity; bedNumber++) {
                Bed bed = Bed.builder()
                        .bedNumber(bedNumber)
                        .isOccupied(false)
                        .room(room)
                        .student(null)
                        .build();
                bedRepository.save(bed);
            }
        }

        if (existingBeds.size() > capacity) {
            List<Bed> removableBeds = existingBeds.stream()
                    .filter(bed -> bed.getBedNumber() > capacity)
                    .toList();

            boolean occupiedOverLimit = removableBeds.stream().anyMatch(Bed::isOccupied);
            if (occupiedOverLimit) {
                throw new IllegalArgumentException("Cannot reduce room type capacity because some beds are occupied");
            }

            bedRepository.deleteAll(removableBeds);
        }

        updateRoomStatusByOccupancy(room);
    }

    private void updateRoomStatusByOccupancy(Room room) {
        List<Bed> beds = bedRepository.findByRoomIdOrderByBedNumberAsc(room.getId());
        long occupied = beds.stream().filter(Bed::isOccupied).count();

        RoomStatus computedStatus = beds.isEmpty()
                ? RoomStatus.AVAILABLE
                : occupied >= beds.size() ? RoomStatus.FULL : RoomStatus.AVAILABLE;

        if (room.getStatus() != RoomStatus.MAINTENANCE && room.getStatus() != computedStatus) {
            room.setStatus(computedStatus);
            roomRepository.save(room);
        }
    }

    private Building findBuildingOrThrow(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
    }

    private RoomType findRoomTypeOrThrow(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + id));
    }

    private String normalizeRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number must not be blank");
        }
        return roomNumber.trim();
    }

    private RoomDTO toRoomDto(Room room) {
        List<Bed> beds = bedRepository.findByRoomIdOrderByBedNumberAsc(room.getId());
        int totalBeds = beds.size();
        int occupiedBeds = (int) beds.stream().filter(Bed::isOccupied).count();

        return RoomDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus() == null ? null : room.getStatus().name())
                .buildingId(room.getBuilding() == null ? null : room.getBuilding().getId())
                .buildingName(room.getBuilding() == null ? null : room.getBuilding().getName())
                .roomTypeId(room.getRoomType() == null ? null : room.getRoomType().getId())
                .roomTypeName(room.getRoomType() == null ? null : room.getRoomType().getName())
                .totalBeds(totalBeds)
                .occupiedBeds(occupiedBeds)
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
