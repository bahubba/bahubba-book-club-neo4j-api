package com.bahubba.bahubbabookclub.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

/** Security configuration for the app */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Sets up security for the application
     *
     * @param httpSecurity HTTP security object for Spring
     * @return SecurityFilterChain
     * @throws Exception If security cannot be configured
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("https://localhost:3000", "https://127.0.0.1:3000"));
                    config.addAllowedHeader("Accept");
                    config.addAllowedHeader("Content-Type");
                    config.addAllowedHeader("X-Requested-With");
                    config.addAllowedHeader("Authorization");
                    config.addAllowedHeader("Access-Control-Allow-Origin");
                    config.setAllowCredentials(true);
                    config.addAllowedMethod("GET");
                    config.addAllowedMethod("PUT");
                    config.addAllowedMethod("PATCH");
                    config.addAllowedMethod("POST");
                    config.addAllowedMethod("DELETE");
                    config.addAllowedMethod("OPTIONS");
                    config.setMaxAge(3600L);
                    return config;
                }))
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/api/v1/auth/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
