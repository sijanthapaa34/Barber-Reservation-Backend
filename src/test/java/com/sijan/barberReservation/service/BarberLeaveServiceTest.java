package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.BarberLeaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarberLeaveServiceTest {

    @Mock
    private BarberLeaveRepository barberLeaveRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BarberLeaveService barberLeaveService;

    @Test
    void findById_Success() {
        // Arrange
        Long leaveId = 1L;
        BarberLeave leave = new BarberLeave();
        leave.setId(leaveId);

        when(barberLeaveRepository.findById(leaveId)).thenReturn(Optional.of(leave));

        // Act
        BarberLeave result = barberLeaveService.findById(leaveId);

        // Assert
        assertNotNull(result);
        assertEquals(leaveId, result.getId());
        verify(barberLeaveRepository, times(1)).findById(leaveId);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long leaveId = 999L;
        when(barberLeaveRepository.findById(leaveId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            barberLeaveService.findById(leaveId);
        });
    }

    @Test
    void applyForLeave_Success() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);

        Admin admin = new Admin();
        admin.setEmail("admin@example.com");
        shop.setAdmin(admin);

        Barber barber = new Barber();
        barber.setId(1L);
        barber.setName("John Barber");
        barber.setBarbershop(shop);

        BarberLeave leave = new BarberLeave();
        leave.setStartDate(LocalDate.now().plusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(3));
        leave.setReason("Personal");

        when(barberLeaveRepository.save(any(BarberLeave.class))).thenReturn(leave);

        // Act
        barberLeaveService.applyForLeave(barber, leave);

        // Assert
        assertEquals(barber, leave.getBarber());
        assertEquals(shop, leave.getBarbershop());
        assertEquals(LeaveStatus.PENDING, leave.getStatus());
        assertNotNull(leave.getRequestedAt());
        verify(barberLeaveRepository, times(1)).save(leave);
        verify(emailService, times(1)).sendLeaveRequestNotificationAdmin(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void approveLeave_Success() {
        // Arrange
        Barber barber = new Barber();
        barber.setEmail("barber@example.com");
        barber.setName("John Barber");

        BarberLeave leave = new BarberLeave();
        leave.setId(1L);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setBarber(barber);
        leave.setStartDate(LocalDate.now().plusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(3));

        when(barberLeaveRepository.save(leave)).thenReturn(leave);

        // Act
        BarberLeave result = barberLeaveService.approveLeave(leave);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        assertNotNull(result.getProcessedAt());
        verify(barberLeaveRepository, times(1)).save(leave);
        verify(emailService, times(1)).sendLeaveApprovalNotification(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void approveLeave_AlreadyProcessed_ThrowsException() {
        // Arrange
        BarberLeave leave = new BarberLeave();
        leave.setStatus(LeaveStatus.APPROVED);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            barberLeaveService.approveLeave(leave);
        });
    }

    @Test
    void rejectLeave_Success() {
        // Arrange
        Barber barber = new Barber();
        barber.setEmail("barber@example.com");
        barber.setName("John Barber");

        BarberLeave leave = new BarberLeave();
        leave.setId(1L);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setBarber(barber);
        leave.setStartDate(LocalDate.now().plusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(3));

        when(barberLeaveRepository.save(leave)).thenReturn(leave);

        // Act
        BarberLeave result = barberLeaveService.rejectLeave(leave);

        // Assert
        assertNotNull(result);
        assertEquals(LeaveStatus.REJECTED, result.getStatus());
        assertNotNull(result.getProcessedAt());
        verify(barberLeaveRepository, times(1)).save(leave);
        verify(emailService, times(1)).sendLeaveRejectionNotification(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void rejectLeave_AlreadyProcessed_ThrowsException() {
        // Arrange
        BarberLeave leave = new BarberLeave();
        leave.setStatus(LeaveStatus.REJECTED);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            barberLeaveService.rejectLeave(leave);
        });
    }

    @Test
    void getLeavesByShop_Success() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        BarberLeave leave1 = new BarberLeave();
        BarberLeave leave2 = new BarberLeave();
        Page<BarberLeave> page = new PageImpl<>(Arrays.asList(leave1, leave2));

        when(barberLeaveRepository.findByBarbershopOrderByRequestedAtDesc(shop, pageable)).thenReturn(page);

        // Act
        Page<BarberLeave> result = barberLeaveService.getLeavesByShop(shop, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(barberLeaveRepository, times(1)).findByBarbershopOrderByRequestedAtDesc(shop, pageable);
    }

    @Test
    void getLeavesByBarber_Success() {
        // Arrange
        Barber barber = new Barber();
        barber.setId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        BarberLeave leave1 = new BarberLeave();
        BarberLeave leave2 = new BarberLeave();
        Page<BarberLeave> page = new PageImpl<>(Arrays.asList(leave1, leave2));

        when(barberLeaveRepository.findByBarberOrderByRequestedAtDesc(barber, pageable)).thenReturn(page);

        // Act
        Page<BarberLeave> result = barberLeaveService.getLeavesByBarber(barber, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(barberLeaveRepository, times(1)).findByBarberOrderByRequestedAtDesc(barber, pageable);
    }

    @Test
    void isOnLeave_True() {
        // Arrange
        Barber barber = new Barber();
        barber.setId(1L);
        LocalDate date = LocalDate.now();

        when(barberLeaveRepository.existsByBarberAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                barber, LeaveStatus.APPROVED, date, date)).thenReturn(true);

        // Act
        boolean result = barberLeaveService.isOnLeave(barber, LeaveStatus.APPROVED, date, date);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOnLeave_False() {
        // Arrange
        Barber barber = new Barber();
        barber.setId(1L);
        LocalDate date = LocalDate.now();

        when(barberLeaveRepository.existsByBarberAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                barber, LeaveStatus.APPROVED, date, date)).thenReturn(false);

        // Act
        boolean result = barberLeaveService.isOnLeave(barber, LeaveStatus.APPROVED, date, date);

        // Assert
        assertFalse(result);
    }
}
