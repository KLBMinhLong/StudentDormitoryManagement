package com.dormitory.management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.room.BedDTO;
import com.dormitory.management.dto.room.BedLayoutRequestDTO;
import com.dormitory.management.dto.room.BedOccupancyRequestDTO;
import com.dormitory.management.dto.room.RoomDTO;
import com.dormitory.management.dto.room.RoomRequestDTO;
import com.dormitory.management.entity.enums.RoomStatus;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.service.RoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<RoomDTO>>> getAllRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<RoomDTO> result = roomService.getAllRooms(keyword, buildingId, status, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get all rooms successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDTO>> getRoomById(@PathVariable Long id) {
        RoomDTO result = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get room successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomDTO>> createRoom(@Valid @RequestBody RoomRequestDTO request) {
        RoomDTO result = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ErrorCode.CREATED.getCode(), "Create room successfully", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDTO>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequestDTO request) {
        RoomDTO result = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Update room successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(ErrorCode.NO_CONTENT.getCode(), "Delete room successfully", null));
    }

    @GetMapping("/{roomId}/beds")
    public ResponseEntity<ApiResponse<PagedResponseDTO<BedDTO>>> getBedsByRoomId(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bedNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        PagedResponseDTO<BedDTO> result = roomService.getBedsByRoomId(roomId, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get beds successfully", result));
    }

    @PutMapping("/{roomId}/beds/{bedId}/occupancy")
    public ResponseEntity<ApiResponse<BedDTO>> updateBedOccupancy(
            @PathVariable Long roomId,
            @PathVariable Long bedId,
            @Valid @RequestBody BedOccupancyRequestDTO request) {
        BedDTO result = roomService.updateBedOccupancy(roomId, bedId, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Update bed occupancy successfully", result));
    }

    @PutMapping("/{roomId}/beds/layout")
    public ResponseEntity<ApiResponse<RoomDTO>> saveBedLayout(
            @PathVariable Long roomId,
            @Valid @RequestBody BedLayoutRequestDTO request) {
        RoomDTO result = roomService.saveBedLayout(roomId, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Save bed layout successfully", result));
    }
}
