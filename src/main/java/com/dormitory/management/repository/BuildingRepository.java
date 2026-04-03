package com.dormitory.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dormitory.management.entity.Building;

public interface BuildingRepository extends JpaRepository<Building, Long> {
	boolean existsByNameIgnoreCase(String name);

	Optional<Building> findByNameIgnoreCase(String name);

	@Query("""
			SELECT b.id,
			       b.name,
			       COUNT(bed.id),
			       COALESCE(SUM(CASE WHEN bed.isOccupied = true THEN 1 ELSE 0 END), 0)
			FROM Building b
			LEFT JOIN b.rooms r
			LEFT JOIN r.beds bed
			GROUP BY b.id, b.name
			ORDER BY b.name ASC
			""")
	List<Object[]> summarizeBedOccupancyByBuilding();
}
