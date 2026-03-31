package com.dormitory.management.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
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
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        })
public class AppUser extends BaseEntity {

        @Column(name = "username", nullable = false, columnDefinition = "nvarchar(50)")
    private String username;

        @Column(name = "password", nullable = false, columnDefinition = "nvarchar(255)")
    private String password;

        @Column(name = "full_name", nullable = false, columnDefinition = "nvarchar(150)")
    private String fullName;

        @Column(name = "email", columnDefinition = "nvarchar(255)")
    private String email;

    @OneToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = {
                    @UniqueConstraint(name = "uk_user_roles_user_role", columnNames = { "user_id", "role_id" })
            })
    private Set<Role> roles = new HashSet<>();
}
