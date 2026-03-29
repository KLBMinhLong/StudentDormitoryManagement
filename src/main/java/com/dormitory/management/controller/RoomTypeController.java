package com.dormitory.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.roomtype.RoomTypeDTO;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.service.RoomTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/room-types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<RoomTypeDTO>>> getAllRoomTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<RoomTypeDTO> result = roomTypeService.getAllRoomTypes(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get all room types successfully", result));
    }
}
