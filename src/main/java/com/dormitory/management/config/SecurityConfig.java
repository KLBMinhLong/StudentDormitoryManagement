package com.dormitory.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dormitory.management.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPoint restAuthenticationEntryPoint;
    private final AccessDeniedHandler restAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationEntryPoint restAuthenticationEntryPoint,
            AccessDeniedHandler restAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/favicon.ico",
                                "/index.html",
                                "/api/v1/auth/register/student",
                                "/api/v1/auth/login",
                                "/login",
                                "/login.html",
                                "/register",
                                "/register.html",
                                "/home",
                                "/my-contracts",
                                "/user/**",
                                "/home.html",
                                "/admin",
                                "/admin/contracts",
                                "/admin/**",
                                "/admin.html",
                                "/buildings",
                                "/buildings.html",
                                "/rooms",
                                "/rooms.html",
                                "/room-detail.html",
                                "/student_management.html",
                                "/student-profile.html",
                                "/ui/**",
                                "/images/**",
                                "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/buildings/**", "/api/v1/rooms/**",
                                "/api/v1/room-types/**")
                        .permitAll()
                        .requestMatchers("/api/v1/payment/webhook/payos")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/pricing-policies/latest")
                        .permitAll()
                        .requestMatchers("/api/v1/buildings/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/pricing-policies/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/invoices/me/**", "/api/v1/invoices/me").hasAuthority("ROLE_STUDENT")
                        .requestMatchers("/api/v1/invoices/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/utility-records/me/**").hasAuthority("ROLE_STUDENT")
                        .requestMatchers("/api/v1/rooms/**", "/api/v1/room-types/**", "/api/v1/utility-records/**")
                        .hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/students/**").hasAnyAuthority("ROLE_STUDENT", "ROLE_ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
