package com.dormitory.management.repository;

import com.dormitory.management.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByRoomIdOrderByBedNumberAsc(Long roomId);

    Optional<Bed> findByIdAndRoomId(Long id, Long roomId);
    
    void deleteByRoomId(Long roomId);
}

