package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dormitory.management.entity.Building;

public interface BuildingRepository extends JpaRepository<Building, Long> {
	boolean existsByNameIgnoreCase(String name);

	Optional<Building> findByNameIgnoreCase(String name);
}
