package com.dormitory.management.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentListItemDTO {

    private Long id;
    private String studentCode;
    private String fullName;
    private String gender;
    private String phone;
    private String email;
    private String avatarUrl;
    private String username;
    private boolean hasAccount;
}
