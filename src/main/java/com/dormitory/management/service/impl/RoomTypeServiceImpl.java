package com.dormitory.management.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.roomtype.RoomTypeDTO;
import com.dormitory.management.entity.RoomType;
import com.dormitory.management.repository.RoomTypeRepository;
import com.dormitory.management.service.RoomTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Override
    public PagedResponseDTO<RoomTypeDTO> getAllRoomTypes(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<RoomTypeDTO> result = roomTypeRepository.findAll(pageable).map(this::toDto);
        return PagedResponseDTO.fromPage(result);
    }

    private RoomTypeDTO toDto(RoomType roomType) {
        return RoomTypeDTO.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .capacity(roomType.getCapacity())
                .basePrice(roomType.getBasePrice())
                .genderAllowed(roomType.getGenderAllowed())
                .build();
    }
}
