package com.dormitory.management.dto.contract;

import com.dormitory.management.entity.enums.ContractChangeType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractChangeRequestCreateDTO {

    @NotNull(message = "Loại yêu cầu là bắt buộc")
    private ContractChangeType changeType;

    private LocalDate requestedEndDate;

    private String reason;
}
