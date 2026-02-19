package com.sijan.barberReservation.config;

import com.sijan.barberReservation.security.JwtAuthenticationFilter;
import com.sijan.barberReservation.security.JwtTokenProvider;
import com.sijan.barberReservation.service.MyUserDetailsService;
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

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. CORS Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Explicit Public Auth Endpoints
                        // We MUST list these individually. If we use "/api/auth/**", it makes /me public too!
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/customer").permitAll()
                        .requestMatchers("/api/auth/google").permitAll()

                        // 3. Public Barbershop Endpoints
                        .requestMatchers(HttpMethod.GET, "/api/barbershop/nearby").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/barbershop/top-rated").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/barbershop/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/barbershop/{id}").permitAll()

                        // 4. Protected Roles
                        .requestMatchers("/api/admin/**").hasAnyRole("MAIN_ADMIN", "SHOP_ADMIN")
                        .requestMatchers("/barber/**").hasRole("BARBER")
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        // Note: Ensure your barbershop update endpoint is protected by role or specific path

                        // 5. Catch-All: ANYTHING ELSE (including /api/auth/me) requires Authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Removed setAllowCredentials(true) for JWT compatibility

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(MyUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }
}