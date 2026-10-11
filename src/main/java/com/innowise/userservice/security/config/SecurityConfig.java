package com.innowise.userservice.security.config;

import com.innowise.userservice.security.filter.JwtAuthFilter;
import com.innowise.userservice.security.handler.JwtAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/me").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/payment-cards/me").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/payment-cards/me").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/payment-cards/*/me").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/payment-cards/*/me").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/payment-cards/*/me").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/payment-cards/*/me").hasRole("USER")
                        .requestMatchers("/api/v1/payment-cards/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN)
                        )
                );

        return http.build();
    }

}
