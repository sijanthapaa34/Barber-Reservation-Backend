package com.sijan.barberReservation.service;

import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AdminRepository;
import com.sijan.barberReservation.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private UserService userService;

    @Mock
    private BarbershopService barbershopService;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void save_BarberShopApplication_Success() {
        // Arrange
        Application application = new Application();
        application.setType(ApplicationType.BARBER_SHOP);
        application.setShopName("Test Shop");
        application.setName("John Admin");
        application.setEmail("admin@example.com");
        application.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(applicationRepository.save(application)).thenReturn(application);
        when(adminRepository.findAllByAdminLevel(AdminLevel.SUPER_ADMIN)).thenReturn(List.of());

        // Act
        Application result = applicationService.save(application);

        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.PENDING, result.getStatus());
        assertEquals("encodedPassword", result.getPassword());
        verify(applicationRepository, times(1)).save(application);
        verify(emailService, times(1)).sendApplicationSubmissionEmail(anyString(), anyString());
    }

    @Test
    void save_BarberApplication_Success() {
        // Arrange
        Application application = new Application();
        application.setType(ApplicationType.BARBER);
        application.setBarbershopName("Test Shop");
        application.setBarbershopId(1L);
        application.setName("John Barber");
        application.setEmail("barber@example.com");
        application.setPassword("password123");

        Barbershop shop = new Barbershop();
        shop.setId(1L);

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(applicationRepository.save(application)).thenReturn(application);
        when(barbershopService.findById(1L)).thenReturn(shop);
        when(adminRepository.findByBarbershop(shop)).thenReturn(Optional.empty());

        // Act
        Application result = applicationService.save(application);

        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.PENDING, result.getStatus());
        verify(applicationRepository, times(1)).save(application);
        verify(emailService, times(1)).sendApplicationSubmissionEmail(anyString(), anyString());
    }

    @Test
    void getRelevantForMainAdmin_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Application app1 = new Application();
        Application app2 = new Application();
        Page<Application> page = new PageImpl<>(Arrays.asList(app1, app2));

        when(applicationRepository.findRelevantForMainAdmin(pageable)).thenReturn(page);

        // Act
        Page<Application> result = applicationService.getRelevantForMainAdmin(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(applicationRepository, times(1)).findRelevantForMainAdmin(pageable);
    }

    @Test
    void getPendingForShopAdmin_Success() {
        // Arrange
        Long barbershopId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Application app1 = new Application();
        Page<Application> page = new PageImpl<>(List.of(app1));

        when(applicationRepository.findByBarbershopIdAndStatus(barbershopId, ApplicationStatus.PENDING, pageable))
                .thenReturn(page);

        // Act
        Page<Application> result = applicationService.getPendingForShopAdmin(barbershopId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void approveByShopAdmin_Success() {
        // Arrange
        Long applicationId = 1L;
        Application app = new Application();
        app.setId(applicationId);
        app.setType(ApplicationType.BARBER);
        app.setStatus(ApplicationStatus.PENDING);
        app.setName("John Barber");
        app.setBarbershopName("Test Shop");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(app)).thenReturn(app);
        when(adminRepository.findAllByAdminLevel(AdminLevel.SUPER_ADMIN)).thenReturn(List.of());

        // Act
        applicationService.approveByShopAdmin(applicationId);

        // Assert
        assertEquals(ApplicationStatus.PENDING_MAIN_APPROVAL, app.getStatus());
        verify(applicationRepository, times(1)).save(app);
    }

    @Test
    void approveByShopAdmin_NotBarberApplication_ThrowsException() {
        // Arrange
        Long applicationId = 1L;
        Application app = new Application();
        app.setType(ApplicationType.BARBER_SHOP);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            applicationService.approveByShopAdmin(applicationId);
        });
    }

    @Test
    void approveByShopAdmin_NotPending_ThrowsException() {
        // Arrange
        Long applicationId = 1L;
        Application app = new Application();
        app.setType(ApplicationType.BARBER);
        app.setStatus(ApplicationStatus.APPROVED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            applicationService.approveByShopAdmin(applicationId);
        });
    }

    @Test
    void approveByMainAdmin_BarberApplication_Success() {
        // Arrange
        Long applicationId = 1L;
        Application app = new Application();
        app.setId(applicationId);
        app.setType(ApplicationType.BARBER);
        app.setStatus(ApplicationStatus.PENDING_MAIN_APPROVAL);
        app.setEmail("barber@example.com");
        app.setBarbershopId(1L);
        app.setBarbershopName("Test Shop");
        app.setName("John Barber");

        Barber barber = new Barber();
        Barbershop shop = new Barbershop();
        shop.setId(1L);
        shop.setName("Test Shop");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(userService.existsByEmail("barber@example.com")).thenReturn(false);
        when(applicationMapper.toBarber(app)).thenReturn(barber);
        when(barbershopService.findById(1L)).thenReturn(shop);
        when(applicationRepository.save(app)).thenReturn(app);
        when(adminRepository.findByBarbershop(shop)).thenReturn(Optional.empty());
        when(adminRepository.findAllByAdminLevel(AdminLevel.SUPER_ADMIN)).thenReturn(List.of());

        // Act
        applicationService.approveByMainAdmin(applicationId);

        // Assert
        assertEquals(ApplicationStatus.APPROVED, app.getStatus());
        verify(userService, times(1)).registerBarberOfApplication(barber, shop);
        verify(emailService, times(1)).sendApplicationStatusEmail(anyString(), anyString(), eq("APPROVED"));
    }

    @Test
    void approveByMainAdmin_ShopApplication_Success() {
        // Arrange
        Long applicationId = 1L;
        Application app = new Application();
        app.setId(applicationId);
        app.setType(ApplicationType.BARBER_SHOP);
        app.setStatus(ApplicationStatus.PENDING);
        app.setEmail("admin@example.com");
        app.setShopName("Test Shop");

        Barbershop shop = new Barbershop();
        Admin admin = new Admin();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(userService.existsByEmail("admin@example.com")).thenReturn(false);
        when(applicationMapper.toBarbershop(app)).thenReturn(shop);
        when(applicationMapper.toAdmin(app)).thenReturn(admin);
        when(applicationRepository.save(app)).thenReturn(app);
        when(adminRepository.findAllByAdminLevel(AdminLevel.SUPER_ADMIN)).thenReturn(List.of());

        // Act
        applicationService.approveByMainAdmin(applicationId);

        // Assert
        assertEquals(ApplicationStatus.APPROVED, app.getStatus());
        verify(barbershopService, times(1)).createBarbershop(shop);
        verify(userService, times(1)).registerAdminOfApplication(admin, shop);
        verify(emailService, times(1)).sendApplicationStatusEmail(anyString(), anyString(), eq("APPROVED"));
    }

    @Test
    void approveByMainAdmin_EmailExists_ThrowsException() {
        // Arrange
        Long applicationId = 1L;
        Application app = new Application();
        app.setEmail("existing@example.com");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            applicationService.approveByMainAdmin(applicationId);
        });
    }

    @Test
    void reject_Success() {
        // Arrange
        Application application = new Application();
        application.setId(1L);
        application.setType(ApplicationType.BARBER_SHOP);
        application.setShopName("Test Shop");
        application.setEmail("admin@example.com");

        when(applicationRepository.save(application)).thenReturn(application);
        when(userService.findByEmail("admin@example.com")).thenReturn(null);

        // Act
        applicationService.reject(application);

        // Assert
        assertEquals(ApplicationStatus.REJECTED, application.getStatus());
        verify(applicationRepository, times(1)).save(application);
        verify(emailService, times(1)).sendApplicationStatusEmail(anyString(), anyString(), eq("REJECTED"));
    }

    @Test
    void findById_Success() {
        // Arrange
        Long applicationId = 1L;
        Application application = new Application();
        application.setId(applicationId);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // Act
        Application result = applicationService.findById(applicationId);

        // Assert
        assertNotNull(result);
        assertEquals(applicationId, result.getId());
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long applicationId = 999L;
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            applicationService.findById(applicationId);
        });
    }
}
