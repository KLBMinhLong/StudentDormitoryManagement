package com.dormitory.management.service;

import com.dormitory.management.dto.building.BuildingDTO;
import com.dormitory.management.dto.building.BuildingRequestDTO;
import com.dormitory.management.dto.common.PagedResponseDTO;

public interface BuildingService {

    PagedResponseDTO<BuildingDTO> getAllBuildings(int page, int size, String sortBy, String direction);

    BuildingDTO getBuildingById(Long id);

    BuildingDTO createBuilding(BuildingRequestDTO request);

    BuildingDTO updateBuilding(Long id, BuildingRequestDTO request);

    void deleteBuilding(Long id);
}
