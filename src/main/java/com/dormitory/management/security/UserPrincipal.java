package com.dormitory.management.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dormitory.management.entity.AppUser;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final String email;
    private final Boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal fromEntity(AppUser appUser) {
        List<SimpleGrantedAuthority> grantedAuthorities = appUser.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .toList();

        return UserPrincipal.builder()
                .id(appUser.getId())
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .fullName(appUser.getFullName())
                .email(appUser.getEmail())
                .enabled(appUser.getEnabled())
                .authorities(grantedAuthorities)
                .build();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
