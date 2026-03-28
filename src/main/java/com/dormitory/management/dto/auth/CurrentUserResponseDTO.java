package com.dormitory.management.dto.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponseDTO {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private List<String> roles;
}
