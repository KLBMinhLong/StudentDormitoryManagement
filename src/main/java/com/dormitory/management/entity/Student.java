package com.dormitory.management.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "student",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_student_code", columnNames = "student_code"),
                @UniqueConstraint(name = "uk_student_cccd", columnNames = "cccd")
        })
public class Student extends BaseEntity {

    @Column(name = "student_code", nullable = false, columnDefinition = "nvarchar(50)")
    private String studentCode;

    @Column(name = "full_name", nullable = false, columnDefinition = "nvarchar(150)")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", columnDefinition = "nvarchar(20)")
    private String gender;

    @Column(name = "phone", columnDefinition = "nvarchar(20)")
    private String phone;

    @Column(name = "cccd", nullable = false, columnDefinition = "nvarchar(20)")
    private String cccd;

    @Column(name = "email", columnDefinition = "nvarchar(120)")
    private String email;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "student", fetch = FetchType.LAZY)
    private Bed bed;

    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "student")
    private List<Contract> contracts = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "student")
    private List<Issue> issues = new ArrayList<>();
}
