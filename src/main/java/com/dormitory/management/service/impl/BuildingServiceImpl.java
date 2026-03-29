package com.dormitory.management.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.building.BuildingDTO;
import com.dormitory.management.dto.building.BuildingRequestDTO;
import com.dormitory.management.entity.Building;
import com.dormitory.management.exception.ResourceNotFoundException;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.service.BuildingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;

    @Override
    public List<BuildingDTO> getAllBuildings() {
        return buildingRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public BuildingDTO getBuildingById(Long id) {
        Building building = findBuildingOrThrow(id);
        return toDto(building);
    }

    @Override
    @Transactional
    public BuildingDTO createBuilding(BuildingRequestDTO request) {
        Building building = new Building();
        building.setName(request.getName().trim());
        building.setTotalFloors(request.getTotalFloors());
        building.setDescription(request.getDescription());

        Building saved = buildingRepository.save(building);
        return toDto(saved);
    }

    @Override
    @Transactional
    public BuildingDTO updateBuilding(Long id, BuildingRequestDTO request) {
        Building existing = findBuildingOrThrow(id);
        existing.setName(request.getName().trim());
        existing.setTotalFloors(request.getTotalFloors());
        existing.setDescription(request.getDescription());

        Building updated = buildingRepository.save(existing);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void deleteBuilding(Long id) {
        Building existing = findBuildingOrThrow(id);
        buildingRepository.delete(existing);
    }

    private Building findBuildingOrThrow(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
    }

    private BuildingDTO toDto(Building building) {
        return BuildingDTO.builder()
                .id(building.getId())
                .name(building.getName())
                .totalFloors(building.getTotalFloors())
                .description(building.getDescription())
                .createdAt(building.getCreatedAt())
                .updatedAt(building.getUpdatedAt())
                .build();
    }
}
