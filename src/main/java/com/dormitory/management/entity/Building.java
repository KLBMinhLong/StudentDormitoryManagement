package com.dormitory.management.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "building")
public class Building extends BaseEntity {

    @Column(name = "name", nullable = false, columnDefinition = "nvarchar(150)")
    private String name;

    @Column(name = "total_floors", nullable = false)
    private int totalFloors;

    @Column(name = "gender_allowed", nullable = false, columnDefinition = "nvarchar(20)")
    private String genderAllowed;

    @Column(name = "description", columnDefinition = "nvarchar(1000)")
    private String description;

    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "building")
    private List<Room> rooms = new ArrayList<>();
}
