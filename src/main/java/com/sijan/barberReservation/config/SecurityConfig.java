package com.sijan.barberReservation.config;

import com.sijan.barberReservation.security.JwtAuthenticationFilter;
import com.sijan.barberReservation.security.JwtTokenProvider;
import com.sijan.barberReservation.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;

    // 1. MANUALLY CREATE THE BEAN HERE
    // This fixes the "could not be found" error
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(MyUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                // Disable CSRF (Required for Login POST to work)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization Rules
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/error").permitAll()
                            // Allow Preflight
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                            // Public Endpoints
                            .requestMatchers("/api/auth/login").permitAll()
                            .requestMatchers("/api/auth/customer").permitAll()
                            .requestMatchers("/api/auth/google").permitAll()
                            .requestMatchers("/api/email/**").permitAll()
                            .requestMatchers("/api/upload/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/applications/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/applications").permitAll()                            .requestMatchers("/api/users/").permitAll()

                            // Public Shop Endpoints
                            .requestMatchers(HttpMethod.GET, "/api/shops/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/barbershop/**").permitAll()

                            // Chat Endpoints (require authentication)
                            .requestMatchers("/api/chat/**").authenticated()
                            .requestMatchers("/api/firestore-chat/**").authenticated()
                            .requestMatchers("/ws/chat/**").permitAll()

                            // Role Protected
                            .requestMatchers("/api/admin/**").hasAnyRole("MAIN_ADMIN", "SHOP_ADMIN")
                            .requestMatchers("/api/barber/**").hasRole("BARBER")
                            .requestMatchers("/api/customer/**").hasRole("CUSTOMER")

                            // Catch-all
                            .anyRequest().authenticated();
                })

                // Add Filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Exception Handling (Returns 401 instead of 403 for missing tokens)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(401, "Unauthorized");
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow your frontend origins
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH","DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}