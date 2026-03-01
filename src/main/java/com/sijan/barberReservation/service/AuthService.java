package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.Auth.AuthRequest;
import com.sijan.barberReservation.DTO.google.GoogleLoginRequest;
import com.sijan.barberReservation.model.LoginHistory;
import com.sijan.barberReservation.model.User;
import com.sijan.barberReservation.repository.LoginHistoryRepository;
import com.sijan.barberReservation.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final JwtTokenProvider tokenProvider;
    private final GoogleTokenVerifierService googleTokenVerifierService;



    public AuthService(AuthenticationManager authenticationManager, UserService userService, LoginHistoryRepository loginHistoryRepository, JwtTokenProvider tokenProvider, GoogleTokenVerifierService googleTokenVerifierService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.loginHistoryRepository = loginHistoryRepository;
        this.tokenProvider = tokenProvider;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    @Transactional
    public String login(AuthRequest request, HttpServletRequest httpRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userService.findByEmail(request.getEmail());
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        user.setLastLogin(LocalDateTime.now());
        recordLogin(user, httpRequest);

        return generateJwt(user);
    }

    @Transactional
    public String loginWithGoogle(GoogleLoginRequest request, HttpServletRequest httpRequest) {

        try {
            var payload = googleTokenVerifierService.verifyToken(request.getIdToken());

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            User user = userService.findByEmail(email);
            if (user == null) {
                user = userService.registerGoogleCustomer(email, name, picture);
            }

            LocalDateTime now = LocalDateTime.now();
            user.setLastLogin(now);

            recordLogin(user, httpRequest);

            return generateJwt(user);

        } catch (Exception e) {
            throw new RuntimeException("Invalid Google token");
        }
    }

    private void recordLogin(User user, HttpServletRequest request) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setLoginTime(user.getLastLogin());
        history.setIpAddress(extractClientIp(request));
        history.setUserAgent(request.getHeader("User-Agent"));
        loginHistoryRepository.save(history);
    }

    private String generateJwt(User user) {
        return tokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().toString()
        );
    }
    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
