package com.dormitory.management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.room.RoomDTO;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.service.RoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getAllRooms() {
        List<RoomDTO> result = roomService.getAllRooms();
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get all rooms successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDTO>> getRoomById(@PathVariable Long id) {
        RoomDTO result = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get room successfully", result));
    }

    @GetMapping("/{roomId}/beds")
    public ResponseEntity<ApiResponse<List>> getBedsByRoomId(@PathVariable Long roomId) {
        List result = roomService.getBedsByRoomId(roomId);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get beds successfully", result));
    }
}
