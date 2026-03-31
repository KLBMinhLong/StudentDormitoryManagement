package com.dormitory.management.dto.building;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingRequestDTO {

    @NotBlank(message = "Tên tòa nhà không được để trống")
    @Size(max = 150, message = "Tên tòa nhà tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Số tầng không được để trống")
    @Positive(message = "Số tầng phải lớn hơn 0")
    private Integer totalFloors;

    @NotBlank(message = "Quy định giới tính không được để trống")
    @Pattern(regexp = "^(Nam|Nữ|Nam/Nữ)$", message = "Quy định giới tính phải là Nam, Nữ, hoặc Nam/Nữ")
    private String genderAllowed;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;
}
