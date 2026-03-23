package com.dormitory.management.service;

import java.util.List;

import com.dormitory.management.dto.building.BuildingDTO;
import com.dormitory.management.dto.building.BuildingRequestDTO;

public interface BuildingService {

    List<BuildingDTO> getAllBuildings();

    BuildingDTO getBuildingById(Long id);

    BuildingDTO createBuilding(BuildingRequestDTO request);

    BuildingDTO updateBuilding(Long id, BuildingRequestDTO request);

    void deleteBuilding(Long id);
}
