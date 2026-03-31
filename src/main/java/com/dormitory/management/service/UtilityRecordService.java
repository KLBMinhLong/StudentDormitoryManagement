package com.dormitory.management.service;

import java.util.List;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.utility.UtilityRecordBatchRequestDTO;
import com.dormitory.management.dto.utility.UtilityRecordDTO;
import com.dormitory.management.dto.utility.UtilityRecordRequestDTO;

public interface UtilityRecordService {

    UtilityRecordDTO createUtilityRecord(UtilityRecordRequestDTO request);

    List<UtilityRecordDTO> createUtilityRecordsBatch(UtilityRecordBatchRequestDTO request);

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
}
