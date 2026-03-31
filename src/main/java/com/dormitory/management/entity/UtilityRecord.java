package com.dormitory.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.dormitory.management.entity.enums.UtilityRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "utility_record")
public class UtilityRecord extends BaseEntity {

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "old_electric")
    private Double oldElectric;

    @Column(name = "new_electric")
    private Double newElectric;

    @Column(name = "old_water")
    private Double oldWater;

    @Column(name = "new_water")
    private Double newWater;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "period_status", nullable = false, length = 20)
    private UtilityRecordStatus periodStatus = UtilityRecordStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
