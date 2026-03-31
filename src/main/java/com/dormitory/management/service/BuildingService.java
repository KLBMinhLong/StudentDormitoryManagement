package com.dormitory.management.service;

import com.dormitory.management.dto.building.BuildingRequestDTO;
import com.dormitory.management.dto.building.BuildingResponseDTO;
import com.dormitory.management.dto.common.PagedResponseDTO;

public interface BuildingService {

    PagedResponseDTO<BuildingResponseDTO> getAllBuildings(int page, int size, String sortBy, String direction);

    BuildingResponseDTO getBuildingById(Long id);

    BuildingResponseDTO createBuilding(BuildingRequestDTO request);

    BuildingResponseDTO updateBuilding(Long id, BuildingRequestDTO request);

    void deleteBuilding(Long id);
}
