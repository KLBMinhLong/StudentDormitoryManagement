package com.dormitory.management.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.utility.UtilityRecordBatchRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordDTO;
import com.dormitory.management.dto.utility.UtilityRecordRequestDTO;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.UtilityRecord;
import com.dormitory.management.exception.ResourceNotFoundException;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.repository.UtilityRecordRepository;
import com.dormitory.management.service.UtilityRecordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilityRecordServiceImpl implements UtilityRecordService {

    private final UtilityRecordRepository utilityRecordRepository;
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

    @Override
    @Transactional
    public UtilityRecordDTO createUtilityRecord(UtilityRecordRequestDTO request) {
        return saveOrUpdateRecord(request, null);
    }

    @Override
    @Transactional
    public List<UtilityRecordDTO> createUtilityRecordsBatch(UtilityRecordBatchRequestDTO request) {
        Long buildingId = request.getBuildingId();
        if (buildingId != null) {
            findBuildingOrThrow(buildingId);
        }

        return request.getRecords().stream()
                .map(record -> saveOrUpdateRecord(record, buildingId))
                .toList();
    }

    @Override
    public PagedResponseDTO<UtilityRecordDTO> searchUtilityRecords(
            Long roomId,
            Long buildingId,
            Integer month,
            Integer year,
            Integer fromMonth,
            Integer fromYear,
            Integer toMonth,
            Integer toYear,
            int page,
            int size,
            String sortBy,
            String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Integer normalizedFromMonth = fromMonth;
        Integer normalizedToMonth = toMonth;
        if (fromYear != null && normalizedFromMonth == null) {
            normalizedFromMonth = 1;
        }
        if (toYear != null && normalizedToMonth == null) {
            normalizedToMonth = 12;
        }

        Page<UtilityRecordDTO> result = utilityRecordRepository.searchUtilityRecords(
                roomId,
                buildingId,
                month,
                year,
                fromYear,
                normalizedFromMonth,
                toYear,
                normalizedToMonth,
                pageable)
                .map(this::mapToDto);

        return PagedResponseDTO.fromPage(result);
    }

    private UtilityRecordDTO saveOrUpdateRecord(UtilityRecordRequestDTO request, Long buildingId) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));

        if (buildingId != null && !room.getBuilding().getId().equals(buildingId)) {
            throw new IllegalArgumentException(
                    "Room " + request.getRoomId() + " does not belong to the selected building");
        }

        validateIndexes(request);

        UtilityRecord record = utilityRecordRepository.findByRoomIdAndMonthAndYear(
                room.getId(), request.getMonth(), request.getYear())
                .orElse(UtilityRecord.builder()
                        .room(room)
                        .month(request.getMonth())
                        .year(request.getYear())
                        .build());

        record.setOldElectric(request.getOldElectric());
        record.setNewElectric(request.getNewElectric());
        record.setOldWater(request.getOldWater());
        record.setNewWater(request.getNewWater());
        record.setRoom(room);

        UtilityRecord saved = utilityRecordRepository.save(record);
        return mapToDto(saved);
    }

    private void validateIndexes(UtilityRecordRequestDTO request) {
        if (request.getNewElectric() < request.getOldElectric()) {
            throw new IllegalArgumentException("Chỉ số điện mới phải lớn hơn hoặc bằng chỉ số điện cũ");
        }
        if (request.getNewWater() < request.getOldWater()) {
            throw new IllegalArgumentException("Chỉ số nước mới phải lớn hơn hoặc bằng chỉ số nước cũ");
        }
    }

    private UtilityRecordDTO mapToDto(UtilityRecord record) {
        double electricConsumption = record.getNewElectric() - record.getOldElectric();
        double waterConsumption = record.getNewWater() - record.getOldWater();

        return UtilityRecordDTO.builder()
                .id(record.getId())
                .roomId(record.getRoom().getId())
                .roomNumber(record.getRoom().getRoomNumber())
                .buildingId(record.getRoom().getBuilding().getId())
                .buildingName(record.getRoom().getBuilding().getName())
                .month(record.getMonth())
                .year(record.getYear())
                .oldElectric(record.getOldElectric())
                .newElectric(record.getNewElectric())
                .electricConsumption(electricConsumption)
                .oldWater(record.getOldWater())
                .newWater(record.getNewWater())
                .waterConsumption(waterConsumption)
                .createdAt(record.getCreatedAt())
                .build();
    }

    private Building findBuildingOrThrow(Long buildingId) {
        return buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + buildingId));
    }
}
