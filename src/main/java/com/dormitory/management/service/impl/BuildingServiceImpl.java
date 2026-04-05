package com.dormitory.management.service.impl;

import static com.dormitory.management.config.CacheConfig.BUILDINGS_LIST_CACHE;
import static com.dormitory.management.config.CacheConfig.BUILDING_BY_ID_CACHE;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.building.BuildingRequestDTO;
import com.dormitory.management.dto.building.BuildingResponseDTO;
import com.dormitory.management.dto.common.PagedResponseDTO;
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
    @Cacheable(cacheNames = BUILDINGS_LIST_CACHE)
    public PagedResponseDTO<BuildingResponseDTO> getAllBuildings(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<BuildingResponseDTO> result = buildingRepository.findAll(pageable).map(this::toDto);
        return PagedResponseDTO.fromPage(result);
    }

    @Override
    @Cacheable(cacheNames = BUILDING_BY_ID_CACHE, key = "#id")
    public BuildingResponseDTO getBuildingById(Long id) {
        Building building = findBuildingOrThrow(id);
        return toDto(building);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {BUILDINGS_LIST_CACHE, BUILDING_BY_ID_CACHE}, allEntries = true)
    public BuildingResponseDTO createBuilding(BuildingRequestDTO request) {
        Building building = new Building();
        building.setName(request.getName().trim());
        building.setTotalFloors(request.getTotalFloors());
        building.setGenderAllowed(request.getGenderAllowed().trim());
        building.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Building saved = buildingRepository.save(building);
        return toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {BUILDINGS_LIST_CACHE, BUILDING_BY_ID_CACHE}, allEntries = true)
    public BuildingResponseDTO updateBuilding(Long id, BuildingRequestDTO request) {
        Building existing = findBuildingOrThrow(id);
        existing.setName(request.getName().trim());
        existing.setTotalFloors(request.getTotalFloors());
        existing.setGenderAllowed(request.getGenderAllowed().trim());
        existing.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Building updated = buildingRepository.save(existing);
        return toDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {BUILDINGS_LIST_CACHE, BUILDING_BY_ID_CACHE}, allEntries = true)
    public void deleteBuilding(Long id) {
        Building existing = findBuildingOrThrow(id);
        buildingRepository.delete(existing);
    }

    private Building findBuildingOrThrow(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tòa nhà không tồn tại với id: " + id));
    }

    private BuildingResponseDTO toDto(Building building) {
        return BuildingResponseDTO.builder()
                .id(building.getId())
                .name(building.getName())
                .totalFloors(building.getTotalFloors())
                .genderAllowed(building.getGenderAllowed())
                .description(building.getDescription())
                .createdAt(building.getCreatedAt())
                .updatedAt(building.getUpdatedAt())
                .build();
    }
}

