package com.dormitory.management.repository;

import com.dormitory.management.entity.Bed;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByRoomIdOrderByBedNumberAsc(Long roomId);

    Optional<Bed> findByIdAndRoomId(Long id, Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b where b.id = :id")
    Optional<Bed> findByIdForUpdate(@Param("id") Long id);

    void deleteByRoomId(Long roomId);

    boolean existsByRoomIdAndIsOccupiedFalse(Long roomId);
}