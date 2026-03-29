package com.dormitory.management.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dormitory.management.dto.building.BuildingDTO;
import com.dormitory.management.dto.building.BuildingRequestDTO;
import com.dormitory.management.entity.Building;
import com.dormitory.management.exception.ResourceNotFoundException;
import com.dormitory.management.repository.BuildingRepository;
import com.dormitory.management.service.impl.BuildingServiceImpl;

@ExtendWith(MockitoExtension.class)
class BuildingServiceImplTest {

    @Mock
    private BuildingRepository buildingRepository;

    @InjectMocks
    private BuildingServiceImpl buildingService;

    private Building mockBuilding;
    private BuildingRequestDTO mockRequest;

    @BeforeEach
    void setUp() {
        mockBuilding = Building.builder()
                .name("Tòa A")
            .totalFloors(5)
                .description("Mô tả tòa A")
                .build();

        mockRequest = BuildingRequestDTO.builder()
                .name("Tòa A")
            .totalFloors(5)
                .description("Mô tả tòa A")
                .build();
    }

    // ── getAllBuildings ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllBuildings - trả về danh sách DTO khi có dữ liệu")
    void getAllBuildings_returnsDtoList() {
        when(buildingRepository.findAll()).thenReturn(List.of(mockBuilding));

        List<BuildingDTO> result = buildingService.getAllBuildings();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Tòa A");
        assertThat(result.get(0).getTotalFloors()).isEqualTo(5);
        assertThat(result.get(0).getDescription()).isEqualTo("Mô tả tòa A");
    }

    @Test
    @DisplayName("getAllBuildings - trả về danh sách rỗng khi không có dữ liệu")
    void getAllBuildings_returnsEmptyList() {
        when(buildingRepository.findAll()).thenReturn(List.of());

        List<BuildingDTO> result = buildingService.getAllBuildings();

        assertThat(result).isEmpty();
    }

    // ── getBuildingById ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getBuildingById - tìm thấy tòa nhà, trả về DTO đúng")
    void getBuildingById_found_returnsDto() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(mockBuilding));

        BuildingDTO result = buildingService.getBuildingById(1L);

        assertThat(result.getName()).isEqualTo("Tòa A");
        assertThat(result.getTotalFloors()).isEqualTo(5);
        assertThat(result.getDescription()).isEqualTo("Mô tả tòa A");
    }

    @Test
    @DisplayName("getBuildingById - không tìm thấy, ném ResourceNotFoundException")
    void getBuildingById_notFound_throwsException() {
        when(buildingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buildingService.getBuildingById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── createBuilding ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createBuilding - tạo mới thành công, trả về DTO")
    void createBuilding_validRequest_returnsDto() {
        when(buildingRepository.save(any(Building.class))).thenReturn(mockBuilding);

        BuildingDTO result = buildingService.createBuilding(mockRequest);

        assertThat(result.getName()).isEqualTo("Tòa A");
        assertThat(result.getTotalFloors()).isEqualTo(5);
        assertThat(result.getDescription()).isEqualTo("Mô tả tòa A");
        verify(buildingRepository).save(any(Building.class));
    }

    @Test
    @DisplayName("createBuilding - tên được trim khoảng trắng trước khi lưu")
    void createBuilding_trimsName() {
        BuildingRequestDTO requestWithSpaces = BuildingRequestDTO.builder()
                .name("  Tòa B  ")
            .totalFloors(6)
                .description(null)
                .build();
        Building trimmedBuilding = Building.builder().name("Tòa B").totalFloors(6).description(null).build();
        when(buildingRepository.save(any(Building.class))).thenReturn(trimmedBuilding);

        BuildingDTO result = buildingService.createBuilding(requestWithSpaces);

        assertThat(result.getName()).isEqualTo("Tòa B");
        assertThat(result.getTotalFloors()).isEqualTo(6);
    }

    // ── updateBuilding ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBuilding - cập nhật thành công, trả về DTO mới")
    void updateBuilding_found_returnsUpdatedDto() {
        BuildingRequestDTO updateRequest = BuildingRequestDTO.builder()
                .name("Tòa B")
            .totalFloors(8)
                .description("Mô tả tòa B")
                .build();
        Building updatedBuilding = Building.builder()
                .name("Tòa B")
            .totalFloors(8)
                .description("Mô tả tòa B")
                .build();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(mockBuilding));
        when(buildingRepository.save(any(Building.class))).thenReturn(updatedBuilding);

        BuildingDTO result = buildingService.updateBuilding(1L, updateRequest);

        assertThat(result.getName()).isEqualTo("Tòa B");
        assertThat(result.getTotalFloors()).isEqualTo(8);
        assertThat(result.getDescription()).isEqualTo("Mô tả tòa B");
        verify(buildingRepository).save(mockBuilding);
    }

    @Test
    @DisplayName("updateBuilding - không tìm thấy tòa nhà, ném ResourceNotFoundException")
    void updateBuilding_notFound_throwsException() {
        when(buildingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buildingService.updateBuilding(99L, mockRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(buildingRepository, never()).save(any());
    }

    // ── deleteBuilding ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBuilding - xoá thành công")
    void deleteBuilding_found_deletesSuccessfully() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(mockBuilding));

        buildingService.deleteBuilding(1L);

        verify(buildingRepository).delete(mockBuilding);
    }

    @Test
    @DisplayName("deleteBuilding - không tìm thấy tòa nhà, ném ResourceNotFoundException")
    void deleteBuilding_notFound_throwsException() {
        when(buildingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buildingService.deleteBuilding(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(buildingRepository, never()).delete(any());
    }
}

