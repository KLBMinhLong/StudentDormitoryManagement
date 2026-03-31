package com.dormitory.management.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.utility.UtilityConsumptionTimelineItemDTO;
import com.dormitory.management.dto.utility.UtilityPeriodLockRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordBatchRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordDTO;
import com.dormitory.management.dto.utility.UtilityRecordPrefillDTO;
import com.dormitory.management.dto.utility.UtilityRecordRequestDTO;
import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Contract;
import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.Student;
import com.dormitory.management.entity.UtilityRecord;
import com.dormitory.management.entity.enums.ContractStatus;
import com.dormitory.management.entity.enums.UtilityRecordStatus;
import com.dormitory.management.exception.ResourceNotFoundException;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.repository.ContractRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.repository.StudentRepository;
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
    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final ContractRepository contractRepository;

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

    @Override
    public List<UtilityConsumptionTimelineItemDTO> getConsumptionTimeline(
            Long roomId,
            Long buildingId,
            Integer fromMonth,
            Integer fromYear,
            Integer toMonth,
            Integer toYear) {
        Integer normalizedFromMonth = fromMonth;
        Integer normalizedToMonth = toMonth;
        if (fromYear != null && normalizedFromMonth == null) {
            normalizedFromMonth = 1;
        }
        if (toYear != null && normalizedToMonth == null) {
            normalizedToMonth = 12;
        }

        List<Object[]> rows = utilityRecordRepository.getConsumptionTimeline(
                roomId,
                buildingId,
                fromYear,
                normalizedFromMonth,
                toYear,
                normalizedToMonth);

        return rows.stream().map(this::mapTimelineRow).toList();
    }

    @Override
    public List<UtilityConsumptionTimelineItemDTO> getMyConsumptionTimeline(
            String username,
            Integer fromMonth,
            Integer fromYear,
            Integer toMonth,
            Integer toYear) {
        Student student = resolveStudentByUsername(username);
        Long roomId = resolveCurrentOrLatestRoomId(student.getId());

        if (roomId == null) {
            return List.of();
        }

        return getConsumptionTimeline(roomId, null, fromMonth, fromYear, toMonth, toYear);
    }

    @Override
    public UtilityRecordPrefillDTO getPrefillForRoom(Long roomId, Integer month, Integer year) {
        if (roomId == null) {
            throw new IllegalArgumentException("Mã phòng không được để trống");
        }
        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }
        if (year == null || year < 2000) {
            throw new IllegalArgumentException("Năm phải hợp lệ");
        }

        roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng không tồn tại với id: " + roomId));

        List<UtilityRecord> records = utilityRecordRepository.findLatestBeforePeriod(
                roomId,
                month,
                year,
                PageRequest.of(0, 1));

        if (records.isEmpty()) {
            return UtilityRecordPrefillDTO.builder()
                    .roomId(roomId)
                    .month(month)
                    .year(year)
                    .hasPreviousRecord(false)
                    .build();
        }

        UtilityRecord previous = records.get(0);
        return UtilityRecordPrefillDTO.builder()
                .roomId(roomId)
                .month(month)
                .year(year)
                .hasPreviousRecord(true)
                .previousMonth(previous.getMonth())
                .previousYear(previous.getYear())
                .suggestedOldElectric(previous.getNewElectric())
                .suggestedOldWater(previous.getNewWater())
                .previousElectricConsumption(calculateConsumption(previous.getOldElectric(), previous.getNewElectric()))
                .previousWaterConsumption(calculateConsumption(previous.getOldWater(), previous.getNewWater()))
                .build();
    }

    @Override
    @Transactional
    public int closePeriod(UtilityPeriodLockRequestDTO request) {
        List<UtilityRecord> records = utilityRecordRepository.findByPeriod(
                request.getBuildingId(),
                request.getMonth(),
                request.getYear());

        if (records.isEmpty()) {
            return 0;
        }

        records.forEach(record -> record.setPeriodStatus(UtilityRecordStatus.CLOSED));
        utilityRecordRepository.saveAll(records);
        return records.size();
    }

    @Override
    @Transactional
    public int reopenPeriod(UtilityPeriodLockRequestDTO request) {
        List<UtilityRecord> records = utilityRecordRepository.findByPeriod(
                request.getBuildingId(),
                request.getMonth(),
                request.getYear());

        if (records.isEmpty()) {
            return 0;
        }

        records.forEach(record -> record.setPeriodStatus(UtilityRecordStatus.OPEN));
        utilityRecordRepository.saveAll(records);
        return records.size();
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

        if (record.getId() != null && isClosed(record)) {
            throw new IllegalStateException("Kỳ đã chốt, không thể chỉnh sửa chỉ số");
        }

        if (record.getPeriodStatus() == null) {
            record.setPeriodStatus(UtilityRecordStatus.OPEN);
        }

        record.setOldElectric(request.getOldElectric());
        record.setNewElectric(request.getNewElectric());
        record.setOldWater(request.getOldWater());
        record.setNewWater(request.getNewWater());
        record.setRoom(room);

        UtilityRecord saved = utilityRecordRepository.save(record);
        return mapToDto(saved);
    }

    private UtilityConsumptionTimelineItemDTO mapTimelineRow(Object[] row) {
        int year = ((Number) row[0]).intValue();
        int month = ((Number) row[1]).intValue();
        double electric = row[2] == null ? 0D : ((Number) row[2]).doubleValue();
        double water = row[3] == null ? 0D : ((Number) row[3]).doubleValue();
        long recordCount = row[4] == null ? 0L : ((Number) row[4]).longValue();

        return UtilityConsumptionTimelineItemDTO.builder()
                .year(year)
                .month(month)
                .periodLabel(String.format("%02d/%d", month, year))
                .totalElectricConsumption(electric)
                .totalWaterConsumption(water)
                .recordCount(recordCount)
                .build();
    }

    private double calculateConsumption(Double oldValue, Double newValue) {
        double oldNum = oldValue == null ? 0D : oldValue;
        double newNum = newValue == null ? 0D : newValue;
        return newNum - oldNum;
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
                .periodStatus(resolveStatus(record).name())
                .createdAt(record.getCreatedAt())
                .build();
    }

    @Override
    public UtilityRecordDTO getUtilityRecordById(Long id) {
        UtilityRecord record = utilityRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chỉ số điện nước không tồn tại với id: " + id));
        return mapToDto(record);
    }

    @Override
    @Transactional
    public UtilityRecordDTO updateUtilityRecord(Long id, UtilityRecordRequestDTO request) {
        UtilityRecord existing = utilityRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chỉ số điện nước không tồn tại với id: " + id));

        if (isClosed(existing)) {
            throw new IllegalStateException("Kỳ đã chốt, không thể cập nhật chỉ số");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Phòng không tồn tại với id: " + request.getRoomId()));

        validateIndexes(request);

        existing.setRoom(room);
        existing.setMonth(request.getMonth());
        existing.setYear(request.getYear());
        existing.setOldElectric(request.getOldElectric());
        existing.setNewElectric(request.getNewElectric());
        existing.setOldWater(request.getOldWater());
        existing.setNewWater(request.getNewWater());

        UtilityRecord updated = utilityRecordRepository.save(existing);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteUtilityRecord(Long id) {
        UtilityRecord existing = utilityRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chỉ số điện nước không tồn tại với id: " + id));

        if (isClosed(existing)) {
            throw new IllegalStateException("Kỳ đã chốt, không thể xóa chỉ số");
        }
        utilityRecordRepository.delete(existing);
    }

    private UtilityRecordStatus resolveStatus(UtilityRecord record) {
        return record.getPeriodStatus() == null ? UtilityRecordStatus.OPEN : record.getPeriodStatus();
    }

    private boolean isClosed(UtilityRecord record) {
        return resolveStatus(record) == UtilityRecordStatus.CLOSED;
    }

    private Building findBuildingOrThrow(Long buildingId) {
        return buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + buildingId));
    }

    private Student resolveStudentByUsername(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        if (appUser.getStudent() != null) {
            return appUser.getStudent();
        }

        return studentRepository.findByStudentCodeIgnoreCase(appUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ sinh viên"));
    }

    private Long resolveCurrentOrLatestRoomId(Long studentId) {
        return contractRepository.findFirstByStudentIdAndStatusOrderByCreatedAtDesc(studentId, ContractStatus.ACTIVE)
                .map(Contract::getRoom)
                .map(Room::getId)
                .or(() -> contractRepository.findFirstByStudentIdAndStatusOrderByCreatedAtDesc(studentId, ContractStatus.EXPIRED)
                        .map(Contract::getRoom)
                        .map(Room::getId))
                .orElse(null);
    }
}
