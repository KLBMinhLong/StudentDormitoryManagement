package com.dormitory.management.service;

import java.util.List;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.utility.UtilityConsumptionTimelineItemDTO;
import com.dormitory.management.dto.utility.UtilityPeriodLockRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordBatchRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordDTO;
import com.dormitory.management.dto.utility.UtilityRecordPrefillDTO;
import com.dormitory.management.dto.utility.UtilityRecordRequestDTO;

public interface UtilityRecordService {

    UtilityRecordDTO createUtilityRecord(UtilityRecordRequestDTO request);

    List<UtilityRecordDTO> createUtilityRecordsBatch(UtilityRecordBatchRequestDTO request);

    UtilityRecordDTO getUtilityRecordById(Long id);

    UtilityRecordDTO updateUtilityRecord(Long id, UtilityRecordRequestDTO request);

    void deleteUtilityRecord(Long id);

    PagedResponseDTO<UtilityRecordDTO> searchUtilityRecords(
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
            String direction);

    List<UtilityConsumptionTimelineItemDTO> getConsumptionTimeline(
            Long roomId,
            Long buildingId,
            Integer fromMonth,
            Integer fromYear,
            Integer toMonth,
            Integer toYear);

    List<UtilityConsumptionTimelineItemDTO> getMyConsumptionTimeline(
            String username,
            Integer fromMonth,
            Integer fromYear,
            Integer toMonth,
            Integer toYear);

        UtilityRecordPrefillDTO getPrefillForRoom(Long roomId, Integer month, Integer year);

        int closePeriod(UtilityPeriodLockRequestDTO request);

        int reopenPeriod(UtilityPeriodLockRequestDTO request);
}
