package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.Auth.AuthRequest;
import com.sijan.barberReservation.DTO.google.GoogleLoginRequest;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.LoginHistoryRepository;
import com.sijan.barberReservation.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private Customer testCustomer;
    private String testToken;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setEmail("customer@test.com");
        testCustomer.setName("Test Customer");
        testCustomer.setRole(Roles.CUSTOMER);

        testToken = "test.jwt.token";

        authRequest = new AuthRequest();
        authRequest.setEmail("customer@test.com");
        authRequest.setPassword("password123");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Test User Agent");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
    }

    @Test
    void login_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByEmail(authRequest.getEmail())).thenReturn(testCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        String result = authService.login(authRequest, httpServletRequest);

        assertNotNull(result);
        assertEquals(testToken, result);
        assertNotNull(testCustomer.getLastLogin());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail(authRequest.getEmail());
        verify(tokenProvider).generateToken(anyString(), anyLong(), anyString());
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void login_UserNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByEmail(authRequest.getEmail())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            authService.login(authRequest, httpServletRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail(authRequest.getEmail());
        verify(tokenProvider, never()).generateToken(anyString(), anyLong(), anyString());
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    void login_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(authRequest, httpServletRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).findByEmail(anyString());
        verify(tokenProvider, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void login_RecordsLoginHistory() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByEmail(authRequest.getEmail())).thenReturn(testCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        authService.login(authRequest, httpServletRequest);

        verify(loginHistoryRepository).save(argThat(history ->
                history.getUser().equals(testCustomer) &&
                        history.getIpAddress().equals("127.0.0.1") &&
                        history.getUserAgent().equals("Test User Agent")
        ));
    }

    @Test
    void login_ExtractsForwardedIp() {
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByEmail(authRequest.getEmail())).thenReturn(testCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        authService.login(authRequest, httpServletRequest);

        verify(loginHistoryRepository).save(argThat(history ->
                history.getIpAddress().equals("192.168.1.1")
        ));
    }

    @Test
    void loginWithGoogle_NewUser_Success() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google.id.token");

        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload =
                mock(com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("newuser@gmail.com");
        when(payload.get("name")).thenReturn("New User");
        when(payload.get("picture")).thenReturn("https://example.com/picture.jpg");

        Customer newCustomer = new Customer();
        newCustomer.setId(2L);
        newCustomer.setEmail("newuser@gmail.com");
        newCustomer.setName("New User");
        newCustomer.setRole(Roles.CUSTOMER);

        when(googleTokenVerifierService.verifyToken(request.getIdToken())).thenReturn(payload);
        when(userService.findByEmail("newuser@gmail.com")).thenReturn(null);
        when(userService.registerGoogleCustomer(anyString(), anyString(), anyString())).thenReturn(newCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        String result = authService.loginWithGoogle(request, httpServletRequest);

        assertNotNull(result);
        assertEquals(testToken, result);
        verify(googleTokenVerifierService).verifyToken(request.getIdToken());
        verify(userService).findByEmail("newuser@gmail.com");
        verify(userService).registerGoogleCustomer("newuser@gmail.com", "New User", "https://example.com/picture.jpg");
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void loginWithGoogle_ExistingUser_Success() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google.id.token");

        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload =
                mock(com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("customer@test.com");
        when(payload.get("name")).thenReturn("Test Customer");
        when(payload.get("picture")).thenReturn("https://example.com/picture.jpg");

        when(googleTokenVerifierService.verifyToken(request.getIdToken())).thenReturn(payload);
        when(userService.findByEmail("customer@test.com")).thenReturn(testCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        String result = authService.loginWithGoogle(request, httpServletRequest);

        assertNotNull(result);
        assertEquals(testToken, result);
        verify(googleTokenVerifierService).verifyToken(request.getIdToken());
        verify(userService).findByEmail("customer@test.com");
        verify(userService, never()).registerGoogleCustomer(anyString(), anyString(), anyString());
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void loginWithGoogle_InvalidToken_ThrowsException() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("invalid.token");

        when(googleTokenVerifierService.verifyToken(request.getIdToken()))
                .thenThrow(new RuntimeException("Invalid token"));

        assertThrows(RuntimeException.class, () -> {
            authService.loginWithGoogle(request, httpServletRequest);
        });

        verify(googleTokenVerifierService).verifyToken(request.getIdToken());
        verify(userService, never()).findByEmail(anyString());
        verify(tokenProvider, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void loginWithGoogle_RecordsLoginHistory() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google.id.token");

        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload =
                mock(com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("customer@test.com");
        when(payload.get("name")).thenReturn("Test Customer");
        when(payload.get("picture")).thenReturn("https://example.com/picture.jpg");

        when(googleTokenVerifierService.verifyToken(request.getIdToken())).thenReturn(payload);
        when(userService.findByEmail("customer@test.com")).thenReturn(testCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        authService.loginWithGoogle(request, httpServletRequest);

        verify(loginHistoryRepository).save(argThat(history ->
                history.getUser().equals(testCustomer) &&
                        history.getIpAddress() != null &&
                        history.getUserAgent() != null
        ));
    }

    @Test
    void login_UpdatesLastLoginTime() {
        LocalDateTime beforeLogin = LocalDateTime.now();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByEmail(authRequest.getEmail())).thenReturn(testCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);
        when(loginHistoryRepository.save(any(LoginHistory.class))).thenReturn(new LoginHistory());

        authService.login(authRequest, httpServletRequest);

        assertNotNull(testCustomer.getLastLogin());
        assertTrue(testCustomer.getLastLogin().isAfter(beforeLogin) ||
                testCustomer.getLastLogin().isEqual(beforeLogin));
    }
}
