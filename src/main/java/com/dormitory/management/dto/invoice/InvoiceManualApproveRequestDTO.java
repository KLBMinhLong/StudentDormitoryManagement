package com.dormitory.management.dto.invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceManualApproveRequestDTO {

    @NotBlank(message = "Ghi chú duyệt thủ công không được để trống")
    @Size(max = 500, message = "Ghi chú duyệt thủ công tối đa 500 ký tự")
    private String note;
}
