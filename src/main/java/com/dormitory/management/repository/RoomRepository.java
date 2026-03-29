package com.dormitory.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dormitory.management.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
