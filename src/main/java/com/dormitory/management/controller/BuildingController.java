package com.dormitory.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.building.BuildingDTO;
import com.dormitory.management.dto.building.BuildingRequestDTO;
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
    public List<BuildingDTO> getAllBuildings() {
        return buildingService.getAllBuildings();
    }

    @GetMapping("/{id}")
    public BuildingDTO getBuildingById(@PathVariable Long id) {
        return buildingService.getBuildingById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuildingDTO createBuilding(@Valid @RequestBody BuildingRequestDTO request) {
        return buildingService.createBuilding(request);
    }

    @PutMapping("/{id}")
    public BuildingDTO updateBuilding(@PathVariable Long id, @Valid @RequestBody BuildingRequestDTO request) {
        return buildingService.updateBuilding(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
    }
}
