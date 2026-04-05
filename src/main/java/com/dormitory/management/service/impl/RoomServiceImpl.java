package com.dormitory.management.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.room.BedDTO;
import com.dormitory.management.dto.room.BedLayoutItemDTO;
import com.dormitory.management.dto.room.BedLayoutRequestDTO;
import com.dormitory.management.dto.room.BedOccupancyRequestDTO;
import com.dormitory.management.dto.room.RoomDTO;
import com.dormitory.management.dto.room.RoomRequestDTO;
import com.dormitory.management.entity.Bed;
import com.dormitory.management.entity.Contract;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.RoomType;
import com.dormitory.management.entity.enums.RoomStatus;
import com.dormitory.management.entity.enums.ContractStatus;
import com.dormitory.management.exception.ResourceNotFoundException;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.repository.ContractRepository;
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
    private final ContractRepository contractRepository;
    private final BuildingRepository buildingRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    public PagedResponseDTO<RoomDTO> getAllRooms(
            String keyword,
            String genderAllowed,
            Long buildingId,
            RoomStatus status,
            int page,
            int size,
            String sortBy,
            String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String normalizedGenderAllowed = (genderAllowed == null || genderAllowed.isBlank()) ? null : genderAllowed.trim();
        Page<Room> roomPage = roomRepository.findByFilters(normalizedGenderAllowed, buildingId, status, normalizedKeyword, pageable);
        Map<Long, int[]> roomStats = summarizeRoomOccupancy(roomPage.getContent());
        Page<RoomDTO> result = roomPage.map((room) -> toRoomDto(room, roomStats.get(room.getId())));
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
                .genderAllowed(resolveRoomGender(building, normalizedRoomNumber))
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
        existing.setGenderAllowed(resolveRoomGender(building, normalizedRoomNumber));
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
    @Transactional
    public PagedResponseDTO<BedDTO> getBedsByRoomId(Long roomId, int page, int size, String sortBy, String direction) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        List<Bed> bedEntities = bedRepository.findByRoomIdOrderByBedNumberAsc(roomId);
        if (bedEntities.isEmpty() && room.getRoomType() != null && room.getRoomType().getCapacity() > 0) {
            // Auto-repair legacy rooms that were created without bed rows.
            syncBedsForRoom(room, room.getRoomType().getCapacity());
            bedEntities = bedRepository.findByRoomIdOrderByBedNumberAsc(roomId);
        }

        List<BedDTO> beds = bedEntities
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

        @Override
        @Transactional
        public BedDTO updateBedOccupancy(Long roomId, Long bedId, BedOccupancyRequestDTO request) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        Bed bed = bedRepository.findByIdAndRoomId(bedId, roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Bed not found with id: " + bedId + " in room: " + roomId));

        bed.setOccupied(Boolean.TRUE.equals(request.getOccupied()));
        if (!bed.isOccupied()) {
            bed.setStudent(null);
            bed.setReservedUntil(null);
            bed.setReservedContractId(null);
        }

        Bed updated = bedRepository.save(bed);
        updateRoomStatusByOccupancy(room);
        return toBedDto(updated);
    }

    @Override
    @Transactional
    public RoomDTO saveBedLayout(Long roomId, BedLayoutRequestDTO request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        List<BedLayoutItemDTO> requestedBeds = request == null || request.getBeds() == null
                ? List.of()
                : request.getBeds();

        if (requestedBeds.size() > 8) {
            throw new IllegalArgumentException("Mỗi phòng chỉ được tối đa 8 giường");
        }

        Set<Integer> positions = new HashSet<>();
        for (BedLayoutItemDTO item : requestedBeds) {
            int bedNumber = item.getBedNumber() == null ? 0 : item.getBedNumber();
            if (bedNumber < 1 || bedNumber > 8) {
                throw new IllegalArgumentException("Vị trí giường phải trong khoảng từ 1 đến 8");
            }
            if (!positions.add(bedNumber)) {
                throw new IllegalArgumentException("Vị trí giường bị trùng, vui lòng kiểm tra lại");
            }
        }

        List<Bed> existingBeds = bedRepository.findByRoomIdOrderByBedNumberAsc(roomId);
        Map<Long, Bed> existingById = new HashMap<>();
        for (Bed bed : existingBeds) {
            existingById.put(bed.getId(), bed);
        }

        Set<Long> incomingIds = new HashSet<>();
        for (BedLayoutItemDTO item : requestedBeds) {
            if (item.getId() == null) {
                continue;
            }

            Bed existing = existingById.get(item.getId());
            if (existing == null) {
                throw new IllegalArgumentException("Giường không tồn tại trong phòng hiện tại");
            }

            incomingIds.add(item.getId());
            boolean occupied = existing.isOccupied() || existing.getStudent() != null;
            if (occupied && existing.getBedNumber() != item.getBedNumber()) {
                throw new IllegalArgumentException("Không thể đổi vị trí giường đã có sinh viên đăng ký");
            }
        }

        List<Bed> removableBeds = new ArrayList<>();
        for (Bed bed : existingBeds) {
            if (incomingIds.contains(bed.getId())) {
                continue;
            }
            boolean occupied = bed.isOccupied() || bed.getStudent() != null;
            if (occupied) {
                throw new IllegalArgumentException("Không thể xóa giường đã có sinh viên đăng ký");
            }
            removableBeds.add(bed);
        }

        if (!removableBeds.isEmpty()) {
            bedRepository.deleteAll(removableBeds);
        }

        for (BedLayoutItemDTO item : requestedBeds) {
            if (item.getId() != null) {
                Bed existing = existingById.get(item.getId());
                existing.setBedNumber(item.getBedNumber());
                bedRepository.save(existing);
                continue;
            }

            Bed newBed = Bed.builder()
                    .bedNumber(item.getBedNumber())
                    .isOccupied(false)
                    .reservedUntil(null)
                    .reservedContractId(null)
                    .room(room)
                    .student(null)
                    .build();
            bedRepository.save(newBed);
        }

        updateRoomStatusByOccupancy(room);
        return getRoomById(roomId);
    }

    private void syncBedsForRoom(Room room, int capacity) {
        List<Bed> existingBeds = new ArrayList<>(bedRepository.findByRoomIdOrderByBedNumberAsc(room.getId()));
        existingBeds.sort(Comparator.comparingInt(Bed::getBedNumber));

        if (existingBeds.size() < capacity) {
            for (int bedNumber = existingBeds.size() + 1; bedNumber <= capacity; bedNumber++) {
                Bed bed = Bed.builder()
                        .bedNumber(bedNumber)
                        .isOccupied(false)
                    .reservedUntil(null)
                    .reservedContractId(null)
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
        LocalDateTime now = LocalDateTime.now();
        long occupied = beds.stream().filter((bed) -> isOccupiedOrReserved(bed, now)).count();

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

    private Map<Long, int[]> summarizeRoomOccupancy(Collection<Room> rooms) {
        Map<Long, int[]> stats = new HashMap<>();
        if (rooms == null || rooms.isEmpty()) {
            return stats;
        }

        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> rows = bedRepository.summarizeBedOccupancyByRoomIds(
                roomIds,
            now);

        for (Object[] row : rows) {
            Long roomId = row[0] == null ? null : ((Number) row[0]).longValue();
            if (roomId == null) {
                continue;
            }
            int totalBeds = row[1] == null ? 0 : ((Number) row[1]).intValue();
            int occupiedBeds = row[2] == null ? 0 : ((Number) row[2]).intValue();
            stats.put(roomId, new int[]{totalBeds, occupiedBeds});
        }

        return stats;
    }

    private RoomDTO toRoomDto(Room room) {
        return toRoomDto(room, null);
    }

    private RoomDTO toRoomDto(Room room, int[] stat) {
        int totalBeds = stat == null ? 0 : stat[0];
        int occupiedBeds = stat == null ? 0 : stat[1];

        return RoomDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus() == null ? null : room.getStatus().name())
                .buildingId(room.getBuilding() == null ? null : room.getBuilding().getId())
                .buildingName(room.getBuilding() == null ? null : room.getBuilding().getName())
                .roomTypeId(room.getRoomType() == null ? null : room.getRoomType().getId())
                .roomTypeName(room.getRoomType() == null ? null : room.getRoomType().getName())
                .genderAllowed(room.getGenderAllowed())
                .totalBeds(totalBeds)
                .occupiedBeds(occupiedBeds)
                .beds(null)
                .build();
    }

    private String resolveRoomGender(Building building, String roomNumber) {
        if (building == null || building.getGenderAllowed() == null) {
            return "Nam/Nữ";
        }

        String buildingGender = building.getGenderAllowed().trim();
        if (!"Nam/Nữ".equalsIgnoreCase(buildingGender)) {
            return buildingGender;
        }

        int floor = extractFloorFromRoomNumber(roomNumber);
        if (floor <= 0) {
            return "Nam/Nữ";
        }

        return floor % 2 == 0 ? "Nam" : "Nữ";
    }

    private int extractFloorFromRoomNumber(String roomNumber) {
        if (!StringUtils.hasText(roomNumber) || roomNumber.length() < 3) {
            return -1;
        }

        String floorPart = roomNumber.substring(1, roomNumber.length() - 2);
        try {
            return Integer.parseInt(floorPart);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private BedDTO toBedDto(Bed bed) {
        LocalDateTime now = LocalDateTime.now();
        Contract currentContract = findCurrentBedContract(bed);
        boolean reserved = isReserved(bed, currentContract, now);
        boolean occupied = currentContract != null && currentContract.getStatus() == ContractStatus.ACTIVE;
        String studentName = currentContract != null && currentContract.getStudent() != null
                ? currentContract.getStudent().getFullName()
                : null;

        String occupancyStatus = reserved
            ? "RESERVED"
            : (occupied ? "OCCUPIED" : "AVAILABLE");

        return BedDTO.builder()
                .id(bed.getId())
                .bedNumber(bed.getBedNumber())
                .isOccupied(occupied)
                .studentName(studentName)
            .occupancyStatus(occupancyStatus)
            .reservedUntil(bed.getReservedUntil())
                .build();
    }

    private boolean isReserved(Bed bed, Contract currentContract, LocalDateTime now) {
        return currentContract != null
                && currentContract.getStatus() == ContractStatus.PENDING
                && bed.getReservedUntil() != null
                && bed.getReservedUntil().isAfter(now);
    }

    private boolean isOccupiedOrReserved(Bed bed, LocalDateTime now) {
        Contract currentContract = findCurrentBedContract(bed);
        return currentContract != null
                || (bed.getReservedUntil() != null && bed.getReservedUntil().isAfter(now));
    }

    private Contract findCurrentBedContract(Bed bed) {
        return contractRepository.findFirstByBedIdAndStatusInOrderByCreatedAtDesc(
                bed.getId(),
                Set.of(ContractStatus.ACTIVE, ContractStatus.PENDING)
        ).orElse(null);
    }
}
