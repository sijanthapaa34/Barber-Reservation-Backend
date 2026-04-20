package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.Auth.AuthRequest;
import com.sijan.barberReservation.DTO.Auth.RegisterBarbershopRequest;
import com.sijan.barberReservation.DTO.Auth.RegisterBarberRequest;
import com.sijan.barberReservation.DTO.Auth.RegisterCustomerRequest;
import com.sijan.barberReservation.DTO.google.GoogleLoginRequest;
import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.mapper.user.*;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.security.JwtTokenProvider;
import com.sijan.barberReservation.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private UserService userService;

    @MockBean
    private BarbershopService barbershopService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private BarberMapper barberMapper;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private AdminMapper adminMapper;

    @MockBean
    private BarbershopMapper barbershopMapper;

    @MockBean
    private CustomerMapper customerMapper;

    private Customer testCustomer;
    private Barber testBarber;
    private Admin testAdmin;
    private Barbershop testBarbershop;
    private String testToken;

    @BeforeEach
    void setUp() {
        testToken = "test.jwt.token";

        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setEmail("customer@test.com");
        testCustomer.setName("Test Customer");
        testCustomer.setRole(Roles.CUSTOMER);

        testBarber = new Barber();
        testBarber.setId(2L);
        testBarber.setEmail("barber@test.com");
        testBarber.setName("Test Barber");
        testBarber.setRole(Roles.BARBER);

        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Shop");

        testAdmin = new Admin();
        testAdmin.setId(3L);
        testAdmin.setEmail("admin@test.com");
        testAdmin.setName("Test Admin");
        testAdmin.setRole(Roles.SHOP_ADMIN);
        testAdmin.setBarbershop(testBarbershop);
    }

    @Test
    void login_Success() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("customer@test.com");
        request.setPassword("password123");

        when(authService.login(any(AuthRequest.class), any(HttpServletRequest.class)))
                .thenReturn(testToken);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(testToken))
                .andExpect(header().exists("Authorization"));

        verify(authService).login(any(AuthRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("wrong@test.com");
        request.setPassword("wrongpassword");

        when(authService.login(any(AuthRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(authService).login(any(AuthRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void loginWithGoogle_Success() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google.id.token");

        when(authService.loginWithGoogle(any(GoogleLoginRequest.class), any(HttpServletRequest.class)))
                .thenReturn(testToken);

        mockMvc.perform(post("/api/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(testToken))
                .andExpect(header().exists("Authorization"));

        verify(authService).loginWithGoogle(any(GoogleLoginRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void loginWithGoogle_InvalidToken() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("invalid.token");

        when(authService.loginWithGoogle(any(GoogleLoginRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid Google token"));

        mockMvc.perform(post("/api/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(authService).loginWithGoogle(any(GoogleLoginRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void registerCustomer_Success() throws Exception {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setEmail("newcustomer@test.com");
        request.setPassword("password123");
        request.setName("New Customer");
        request.setPhone("1234567890");
        request.setOtp("123456");

        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);
        when(userService.findByEmail(anyString())).thenReturn(null);
        when(customerMapper.toEntity(any(RegisterCustomerRequest.class))).thenReturn(testCustomer);
        when(userService.registerCustomer(any(Customer.class))).thenReturn(testCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);
        doNothing().when(emailService).sendRegistrationConfirmation(anyString(), anyString());

        mockMvc.perform(post("/api/auth/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(testToken))
                .andExpect(header().exists("Authorization"));

        verify(otpService).verifyOtp(request.getEmail(), request.getOtp());
        verify(userService).registerCustomer(any(Customer.class));
        verify(emailService).sendRegistrationConfirmation(anyString(), anyString());
    }

    @Test
    void registerCustomer_InvalidOtp() throws Exception {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setEmail("newcustomer@test.com");
        request.setPassword("password123");
        request.setName("New Customer");
        request.setOtp("000000");

        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/auth/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(otpService).verifyOtp(request.getEmail(), request.getOtp());
        verify(userService, never()).registerCustomer(any());
    }

    @Test
    void registerCustomer_EmailAlreadyExists() throws Exception {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setName("Existing Customer");
        request.setOtp("123456");

        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);
        when(userService.findByEmail(anyString())).thenReturn(testCustomer);

        mockMvc.perform(post("/api/auth/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(userService).findByEmail(request.getEmail());
        verify(userService, never()).registerCustomer(any());
    }

    @Test
    void registerBarber_Success() throws Exception {
        RegisterBarberRequest request = new RegisterBarberRequest();
        request.setEmail("newbarber@test.com");
        request.setPassword("password123");
        request.setName("New Barber");
        request.setPhone("1234567890");

        BarberDTO barberDTO = new BarberDTO();
        barberDTO.setId(2L);
        barberDTO.setEmail("newbarber@test.com");
        barberDTO.setName("New Barber");

        when(barberMapper.toEntity(any(RegisterBarberRequest.class))).thenReturn(testBarber);
        when(barbershopService.findById(1L)).thenReturn(testBarbershop);
        when(userService.registerBarber(any(Barber.class), any(Barbershop.class))).thenReturn(testBarber);
        when(barberMapper.toDTO(any(Barber.class))).thenReturn(barberDTO);
        doNothing().when(emailService).sendRegistrationConfirmation(anyString(), anyString());

        mockMvc.perform(post("/api/auth/barber/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("newbarber@test.com"));

        verify(barbershopService).findById(1L);
        verify(userService).registerBarber(any(Barber.class), any(Barbershop.class));
        verify(emailService).sendRegistrationConfirmation(anyString(), anyString());
    }

    @Test
    void registerBarber_BarbershopNotFound() throws Exception {
        RegisterBarberRequest request = new RegisterBarberRequest();
        request.setEmail("newbarber@test.com");
        request.setPassword("password123");
        request.setName("New Barber");

        when(barberMapper.toEntity(any(RegisterBarberRequest.class))).thenReturn(testBarber);
        when(barbershopService.findById(999L)).thenThrow(new RuntimeException("Barbershop not found"));

        mockMvc.perform(post("/api/auth/barber/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(barbershopService).findById(999L);
        verify(userService, never()).registerBarber(any(), any());
    }

    @Test
    void registerBarbershop_Success() throws Exception {
        RegisterBarbershopRequest request = new RegisterBarbershopRequest();
        request.setShopName("New Barbershop");
        request.setAdminEmail("newshop@test.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setAddress("123 Test St");

        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setId(3L);
        adminDTO.setEmail("newshop@test.com");

        when(barbershopMapper.toEntity(any(RegisterBarbershopRequest.class))).thenReturn(testBarbershop);
        when(adminMapper.toEntity(any(RegisterBarbershopRequest.class))).thenReturn(testAdmin);
        when(barbershopService.createBarbershop(any(Barbershop.class))).thenReturn(testBarbershop);
        when(userService.registerAdmin(any(Admin.class), any(Barbershop.class))).thenReturn(testAdmin);
        when(adminMapper.toDTO(any(Admin.class))).thenReturn(adminDTO);

        mockMvc.perform(post("/api/auth/barbershop")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("newshop@test.com"));

        verify(barbershopService).createBarbershop(any(Barbershop.class));
        verify(userService).registerAdmin(any(Admin.class), any(Barbershop.class));
    }

    @Test
    @WithMockUser
    void getCurrentUser_Success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("customer@test.com");
        userDTO.setName("Test Customer");

        when(userService.findById(anyLong())).thenReturn(testCustomer);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("customer@test.com"));

        verify(userService).findById(anyLong());
        verify(userMapper).toDTO(any(User.class));
    }

    @Test
    @WithMockUser
    void refreshToken_Success() throws Exception {
        when(userService.findById(anyLong())).thenReturn(testCustomer);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyString())).thenReturn(testToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(testToken))
                .andExpect(header().exists("Authorization"));

        verify(userService).findById(anyLong());
        verify(tokenProvider).generateToken(anyString(), anyLong(), anyString());
    }
}
