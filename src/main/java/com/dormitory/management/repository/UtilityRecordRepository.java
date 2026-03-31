package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dormitory.management.entity.UtilityRecord;

public interface UtilityRecordRepository extends JpaRepository<UtilityRecord, Long> {

    Optional<UtilityRecord> findByRoomIdAndMonthAndYear(Long roomId, int month, int year);

    @Query("""
            select ur from UtilityRecord ur
            where (:roomId is null or ur.room.id = :roomId)
              and (:buildingId is null or ur.room.building.id = :buildingId)
              and (:month is null or ur.month = :month)
              and (:year is null or ur.year = :year)
              and (:fromYear is null or (ur.year > :fromYear or (ur.year = :fromYear and ur.month >= :fromMonth)))
              and (:toYear is null or (ur.year < :toYear or (ur.year = :toYear and ur.month <= :toMonth)))
            """)
    Page<UtilityRecord> searchUtilityRecords(
            @Param("roomId") Long roomId,
            @Param("buildingId") Long buildingId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("fromYear") Integer fromYear,
            @Param("fromMonth") Integer fromMonth,
            @Param("toYear") Integer toYear,
            @Param("toMonth") Integer toMonth,
            Pageable pageable);
}
