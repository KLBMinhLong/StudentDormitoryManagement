package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dormitory.management.entity.RoomType;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

	Optional<RoomType> findByNameIgnoreCase(String name);
}
