package com.sijan.barberReservation.security;

import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final MyUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, MyUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        // LOG 1: Did we find a token?
        if (token != null) {
            logger.info("=== JWT Filter: Token found ===");

            // LOG 2: Is the token valid (signature/expiry)?
            if (tokenProvider.validateToken(token)) {
                logger.info("=== JWT Filter: Token is valid ===");

                try {
                    String email = tokenProvider.extractEmail(token);

                    // LOG 3: Who is the user?
                    logger.info("=== JWT Filter: Attempting to load user for email: {} ===", email);

                    UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(email);

                    logger.info("=== JWT Filter: User loaded successfully. Role: {} ===", userPrincipal.getAuthorities());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,
                                    null,
                                    userPrincipal.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("=== JWT Filter: Authentication set in SecurityContext ===");

                } catch (Exception e) {
                    // LOG 4: THIS IS THE LIKELY CULPRIT
                    logger.error("=== JWT Filter: ERROR loading user or setting context! ===", e);
                    // Ensure context is clear if we failed
                    SecurityContextHolder.clearContext();
                }
            } else {
                logger.warn("=== JWT Filter: Token validation failed (Expired or Invalid) ===");
            }
        }

        filterChain.doFilter(request, response);
    }


    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}