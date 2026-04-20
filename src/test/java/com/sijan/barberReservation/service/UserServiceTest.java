package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private Customer testCustomer;
    private Barber testBarber;
    private Admin testAdmin;
    private Barbershop testBarbershop;

    @BeforeEach
    void setUp() {
        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Shop");

        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setEmail("customer@test.com");
        testCustomer.setName("Test Customer");
        testCustomer.setPassword("password123");
        testCustomer.setRole(Roles.CUSTOMER);

        testBarber = new Barber();
        testBarber.setId(2L);
        testBarber.setEmail("barber@test.com");
        testBarber.setName("Test Barber");
        testBarber.setPassword("password123");
        testBarber.setRole(Roles.BARBER);

        testAdmin = new Admin();
        testAdmin.setId(3L);
        testAdmin.setEmail("admin@test.com");
        testAdmin.setName("Test Admin");
        testAdmin.setPassword("password123");
        testAdmin.setRole(Roles.SHOP_ADMIN);
    }

    @Test
    void findByEmail_Success() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(testCustomer);

        User result = userService.findByEmail("customer@test.com");

        assertNotNull(result);
        assertEquals("customer@test.com", result.getEmail());
        verify(userRepository).findByEmail("customer@test.com");
    }

    @Test
    void findByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(null);

        User result = userService.findByEmail("nonexistent@test.com");

        assertNull(result);
        verify(userRepository).findByEmail("nonexistent@test.com");
    }

    @Test
    void registerCustomer_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        Customer result = userService.registerCustomer(testCustomer);

        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository).findByEmail(testCustomer.getEmail());
        verify(passwordEncoder).encode("password123");
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void registerCustomer_EmailAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(testCustomer);

        assertThrows(RuntimeException.class, () -> {
            userService.registerCustomer(testCustomer);
        });

        verify(userRepository).findByEmail(testCustomer.getEmail());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerBarber_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(barberRepository.save(any(Barber.class))).thenReturn(testBarber);
        doNothing().when(emailService).sendBarberCredentials(anyString(), anyString(), anyString(), anyString());

        Barber result = userService.registerBarber(testBarber, testBarbershop);

        assertNotNull(result);
        assertEquals(Roles.BARBER, result.getRole());
        assertEquals(testBarbershop, result.getBarbershop());
        verify(userRepository).existsByEmail(testBarber.getEmail());
        verify(passwordEncoder).encode(anyString());
        verify(barberRepository).save(testBarber);
        verify(emailService).sendBarberCredentials(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void registerBarber_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.registerBarber(testBarber, testBarbershop);
        });

        verify(userRepository).existsByEmail(testBarber.getEmail());
        verify(barberRepository, never()).save(any());
    }

    @Test
    void registerBarber_GeneratesPasswordWhenNull() {
        testBarber.setPassword(null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(barberRepository.save(any(Barber.class))).thenReturn(testBarber);
        doNothing().when(emailService).sendBarberCredentials(anyString(), anyString(), anyString(), anyString());

        Barber result = userService.registerBarber(testBarber, testBarbershop);

        assertNotNull(result);
        verify(passwordEncoder).encode(anyString());
        verify(emailService).sendBarberCredentials(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void registerAdmin_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);
        doNothing().when(emailService).sendShopAdminCredentials(anyString(), anyString(), anyString(), anyString());

        Admin result = userService.registerAdmin(testAdmin, testBarbershop);

        assertNotNull(result);
        assertEquals(Roles.SHOP_ADMIN, result.getRole());
        assertEquals(testBarbershop, result.getBarbershop());
        verify(userRepository).existsByEmail(testAdmin.getEmail());
        verify(passwordEncoder).encode(anyString());
        verify(adminRepository).save(testAdmin);
        verify(emailService).sendShopAdminCredentials(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void registerAdmin_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.registerAdmin(testAdmin, testBarbershop);
        });

        verify(userRepository).existsByEmail(testAdmin.getEmail());
        verify(adminRepository, never()).save(any());
    }

    @Test
    void registerAdmin_GeneratesPasswordWhenEmpty() {
        testAdmin.setPassword("");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);
        doNothing().when(emailService).sendShopAdminCredentials(anyString(), anyString(), anyString(), anyString());

        Admin result = userService.registerAdmin(testAdmin, testBarbershop);

        assertNotNull(result);
        verify(passwordEncoder).encode(anyString());
        verify(emailService).sendShopAdminCredentials(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void findById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.findById(999L);
        });

        verify(userRepository).findById(999L);
    }

    @Test
    void uploadProfilePicture_Success() {
        String fileUrl = "https://example.com/profile.jpg";

        userService.uploadProfilePicture(testCustomer, fileUrl);

        assertEquals(fileUrl, testCustomer.getProfilePicture());
    }

    @Test
    void registerGoogleCustomer_Success() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        User result = userService.registerGoogleCustomer(
                "google@test.com",
                "Google User",
                "https://example.com/pic.jpg"
        );

        assertNotNull(result);
        verify(passwordEncoder).encode(anyString());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void count_Success() {
        when(userRepository.count()).thenReturn(100L);

        long result = userService.count();

        assertEquals(100L, result);
        verify(userRepository).count();
    }

    @Test
    void countByLastLoginAfter_Success() {
        LocalDateTime dateTime = LocalDateTime.now().minusDays(7);
        when(userRepository.countByLastLoginAfter(dateTime)).thenReturn(50);

        int result = userService.countByLastLoginAfter(dateTime);

        assertEquals(50, result);
        verify(userRepository).countByLastLoginAfter(dateTime);
    }

    @Test
    void existsByEmail_True() {
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        boolean result = userService.existsByEmail("existing@test.com");

        assertTrue(result);
        verify(userRepository).existsByEmail("existing@test.com");
    }

    @Test
    void existsByEmail_False() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        boolean result = userService.existsByEmail("new@test.com");

        assertFalse(result);
        verify(userRepository).existsByEmail("new@test.com");
    }

    @Test
    void registerBarberOfApplication_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(barberRepository.save(any(Barber.class))).thenReturn(testBarber);

        Barber result = userService.registerBarberOfApplication(testBarber, testBarbershop);

        assertNotNull(result);
        assertEquals(Roles.BARBER, result.getRole());
        assertEquals(testBarbershop, result.getBarbershop());
        verify(userRepository).existsByEmail(testBarber.getEmail());
        verify(barberRepository).save(testBarber);
    }

    @Test
    void registerAdminOfApplication_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        Admin result = userService.registerAdminOfApplication(testAdmin, testBarbershop);

        assertNotNull(result);
        assertEquals(Roles.SHOP_ADMIN, result.getRole());
        assertEquals(testBarbershop, result.getBarbershop());
        verify(userRepository).existsByEmail(testAdmin.getEmail());
        verify(adminRepository).save(testAdmin);
    }
}
