package com.example.soop.global.security;

import com.example.soop.global.jwt.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers.frameOptions(
                frameOptionsConfig -> frameOptionsConfig.sameOrigin()))
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(
                auth ->
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                            "/h2-console/**",
                            "/swagger-ui/**",
                            "/swagger-resource",
                            "/v3/api-docs/**",
                            "/api/v1/users/**",
                            "/api/v1/emotion-logs/**",
                            "/api/v1/emotion-report/**",
                            "/api/v1/chat/**",
                            "/ws/chat/**",
                                "/api/v1/knowledge/**",
                                "/api/chat/ask",
                                "/api/mental-tip"
                        ).permitAll()
                        .anyRequest().authenticated()
            )
            // jwt filter
            .addFilterBefore(jwtAuthorizationFilter,
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }
}