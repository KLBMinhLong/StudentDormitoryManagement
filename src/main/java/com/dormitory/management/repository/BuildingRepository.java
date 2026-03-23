package com.dormitory.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dormitory.management.entity.Building;

public interface BuildingRepository extends JpaRepository<Building, Long> {
}
