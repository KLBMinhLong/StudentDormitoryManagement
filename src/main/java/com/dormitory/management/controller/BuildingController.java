package com.dormitory.management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.building.BuildingDTO;
import com.dormitory.management.dto.building.BuildingRequestDTO;
import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.service.BuildingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<BuildingDTO>>> getAllBuildings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PagedResponseDTO<BuildingDTO> result = buildingService.getAllBuildings(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get all buildings successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BuildingDTO>> getBuildingById(@PathVariable Long id) {
        BuildingDTO result = buildingService.getBuildingById(id);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Get building successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BuildingDTO>> createBuilding(@Valid @RequestBody BuildingRequestDTO request) {
        BuildingDTO result = buildingService.createBuilding(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ErrorCode.CREATED.getCode(), "Create building successfully", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BuildingDTO>> updateBuilding(
            @PathVariable Long id,
            @Valid @RequestBody BuildingRequestDTO request) {
        BuildingDTO result = buildingService.updateBuilding(id, request);
        return ResponseEntity.ok(ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Update building successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(ErrorCode.NO_CONTENT.getCode(), "Delete building successfully", null));
    }
}
