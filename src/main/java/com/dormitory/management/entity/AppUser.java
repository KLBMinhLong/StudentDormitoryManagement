package com.dormitory.management.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

        @Column(name = "email", columnDefinition = "nvarchar(120)")
    private String email;

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
