package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.AdminDashboardResponse;
import com.sijan.barberReservation.DTO.user.ShopAdminDashboardResponse;
import com.sijan.barberReservation.exception.admin.AdminNotFoundException;
import com.sijan.barberReservation.exception.auth.InvalidPasswordException;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AdminRepository;
import com.sijan.barberReservation.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private BarbershopService barbershopService;

    @Mock
    private BarberService barberService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ServiceOfferingService serviceService;

    @Mock
    private UserService userService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private BarbershopMapper barbershopMapper;

    @Mock
    private BarberMapper barberMapper;

    @Mock
    private ServiceMapper serviceMapper;

    @Mock
    private AppointmentDetailsMapper appointmentDetailsMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void findById_Success() {
        // Arrange
        Long adminId = 1L;
        Admin admin = new Admin();
        admin.setId(adminId);

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // Act
        Admin result = adminService.findById(adminId);

        // Assert
        assertNotNull(result);
        assertEquals(adminId, result.getId());
        verify(adminRepository, times(1)).findById(adminId);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long adminId = 999L;
        when(adminRepository.findById(adminId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AdminNotFoundException.class, () -> {
            adminService.findById(adminId);
        });
    }

    @Test
    void getDashboardData_Success() {
        // Arrange
        when(userService.count()).thenReturn(100L);
        when(barbershopService.countByActiveTrue()).thenReturn(20L);
        when(appointmentService.count()).thenReturn(500L);
        when(paymentTransactionRepository.sumRevenueByPaidAtBetween(any(), any())).thenReturn(50000.0);
        when(paymentTransactionRepository.sumPlatformFeeByPaidAtBetween(any(), any())).thenReturn(2500.0);
        when(paymentTransactionRepository.sumShopEarningsByPaidAtBetween(any(), any())).thenReturn(47500.0);
        when(userService.countByLastLoginAfter(any())).thenReturn(15L);
        when(barbershopService.findTop4ByActiveTrueOrderByRatingDesc()).thenReturn(List.of());
        when(paymentTransactionRepository.findRecentCompleted(any())).thenReturn(List.of());
        when(barbershopMapper.toDTOs(any())).thenReturn(List.of());

        // Act
        AdminDashboardResponse result = adminService.getDashboardData();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(20L, result.getActiveShops());
        assertEquals(50000.0, result.getMonthlyRevenue());
        assertEquals(500L, result.getTotalBookings());
        verify(userService, times(1)).count();
        verify(barbershopService, times(1)).countByActiveTrue();
    }

    @Test
    void getShopAdminDashboardData_Success() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setBarbershop(shop);

        when(barberService.countByBarbershop(shop)).thenReturn(5);
        when(reviewService.countByBarbershop(shop)).thenReturn(10L);
        when(paymentTransactionRepository.countByBarbershopAndPaidAtBetween(any(), any(), any())).thenReturn(20);
        when(paymentTransactionRepository.sumRevenueByBarbershopAndPaidAtBetween(any(), any(), any())).thenReturn(10000.0);
        when(appointmentService.countByShopAndStatus(any(), any())).thenReturn(5);
        when(barberService.countByBarbershopAndAvailableTrue(shop)).thenReturn(4);
        when(barberService.findTopBarbersByShops(any(), any())).thenReturn(List.of());
        when(serviceService.findPopularServices(any(), any())).thenReturn(List.of());
        when(appointmentService.findUpcomingByShop(any(), any(), any())).thenReturn(List.of());
        when(appointmentDetailsMapper.toDTOs(any())).thenReturn(List.of());

        // Act
        ShopAdminDashboardResponse result = adminService.getShopAdminDashboardData(admin);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getTotalReviews());
        assertEquals(5, result.getTotalBarbers());
        verify(barberService, times(1)).countByBarbershop(shop);
        verify(reviewService, times(1)).countByBarbershop(shop);
    }

    @Test
    void update_Success() {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setName("Old Name");
        admin.setPhone("1111111111");

        String newName = "New Name";
        String newPhone = "2222222222";

        // Act
        Admin result = adminService.update(admin, newName, newPhone);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newPhone, result.getPhone());
    }

    @Test
    void changePassword_Success() {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setPassword("encodedOldPassword");

        String currentPassword = "oldPassword";
        String newPassword = "newPassword";

        when(passwordEncoder.matches(currentPassword, "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // Act
        adminService.changePassword(admin, currentPassword, newPassword);

        // Assert
        assertEquals("encodedNewPassword", admin.getPassword());
        verify(passwordEncoder, times(1)).matches(currentPassword, "encodedOldPassword");
        verify(passwordEncoder, times(1)).encode(newPassword);
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsException() {
        // Arrange
        Admin admin = new Admin();
        admin.setPassword("encodedOldPassword");

        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(passwordEncoder.matches(currentPassword, "encodedOldPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> {
            adminService.changePassword(admin, currentPassword, newPassword);
        });
    }
}
