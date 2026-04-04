package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.enums.RoomStatus;

public interface RoomRepository extends JpaRepository<Room, Long> {

		@EntityGraph(attributePaths = {"building", "roomType"})
		@Query("""
						select r from Room r
						where (:genderAllowed is null or r.genderAllowed = :genderAllowed)
							and (:buildingId is null or r.building.id = :buildingId)
							and (:status is null or r.status = :status)
							and (:keyword is null or r.roomNumber like concat('%', :keyword, '%'))
						""")
		Page<Room> findByFilters(
				@Param("genderAllowed") String genderAllowed,
				@Param("buildingId") Long buildingId,
				@Param("status") RoomStatus status,
				@Param("keyword") String keyword,
				Pageable pageable);

		boolean existsByBuildingIdAndRoomNumberIgnoreCase(Long buildingId, String roomNumber);

		boolean existsByBuildingIdAndRoomNumberIgnoreCaseAndIdNot(Long buildingId, String roomNumber, Long id);

		Optional<Room> findByBuildingIdAndRoomNumberIgnoreCase(Long buildingId, String roomNumber);
}
