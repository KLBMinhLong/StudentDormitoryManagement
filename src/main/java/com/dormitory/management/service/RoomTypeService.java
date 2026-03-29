package com.dormitory.management.service;

import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.dto.roomtype.RoomTypeDTO;

public interface RoomTypeService {

    PagedResponseDTO<RoomTypeDTO> getAllRoomTypes(int page, int size, String sortBy, String direction);
}
