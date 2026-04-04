package com.dormitory.management.repository;

import com.dormitory.management.entity.Bed;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
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

    long countByIsOccupiedTrue();

    @Query("""
            SELECT COUNT(DISTINCT b.room.id)
            FROM Bed b
            WHERE b.isOccupied = true
            """)
    long countDistinctOccupiedRooms();

    @Query(value = """
            SELECT b.room_id,
                   COUNT(b.id),
                   COUNT(DISTINCT CASE
                       WHEN ((b.reserved_until IS NOT NULL AND b.reserved_until > :now)
                           OR c.bed_id IS NOT NULL)
                       THEN b.id
                       ELSE NULL
                   END)
            FROM bed b
            LEFT JOIN contract c
                   ON c.bed_id = b.id
                  AND c.status IN ('ACTIVE', 'PENDING')
            WHERE b.room_id IN (:roomIds)
            GROUP BY b.room_id
            """, nativeQuery = true)
    List<Object[]> summarizeBedOccupancyByRoomIds(
            @Param("roomIds") Collection<Long> roomIds,
            @Param("now") LocalDateTime now);
}