package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.auth.InvalidPasswordException;
import com.sijan.barberReservation.exception.barber.BarberNotFoundException;
import com.sijan.barberReservation.exception.barber.InvalidDateException;
import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.BarberLeaveRepository;
import com.sijan.barberReservation.repository.BarberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarberServiceTest {

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private BarberLeaveRepository barberLeaveRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BarberService barberService;

    private Barber testBarber;
    private Barbershop testBarbershop;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Shop");

        testBarber = new Barber();
        testBarber.setId(1L);
        testBarber.setName("Test Barber");
        testBarber.setPassword("encoded-password");
        testBarber.setBarbershop(testBarbershop);
        testBarber.setActive(true);

        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setBarbershop(testBarbershop);
    }

    @Test
    void findById_Success() {
        when(barberRepository.findById(1L)).thenReturn(Optional.of(testBarber));

        Barber result = barberService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(barberRepository).findById(1L);
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(barberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BarberNotFoundException.class, () -> {
            barberService.findById(1L);
        });
    }

    @Test
    void findByBarberShop_Success() {
        Page<Barber> page = new PageImpl<>(Arrays.asList(testBarber));
        Pageable pageable = PageRequest.of(0, 10);

        when(barberRepository.findByBarbershop(testBarbershop, pageable)).thenReturn(page);

        Page<Barber> result = barberService.findByBarberShop(testBarbershop, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(barberRepository).findByBarbershop(testBarbershop, pageable);
    }

    @Test
    void changePassword_Success() {
        when(passwordEncoder.matches("oldPassword", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("new-encoded-password");

        barberService.changePassword(testBarber, "oldPassword", "newPassword");

        assertEquals("new-encoded-password", testBarber.getPassword());
        verify(passwordEncoder).matches("oldPassword", "encoded-password");
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsException() {
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> {
            barberService.changePassword(testBarber, "wrongPassword", "newPassword");
        });
    }

    @Test
    void applyForLeave_ValidDates_Success() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);

        assertDoesNotThrow(() -> {
            barberService.applyForLeave(testBarber, startDate, endDate, "Vacation");
        });
    }

    @Test
    void applyForLeave_InvalidDates_ThrowsException() {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(1);

        assertThrows(InvalidDateException.class, () -> {
            barberService.applyForLeave(testBarber, startDate, endDate, "Vacation");
        });
    }

    @Test
    void activateBarber_Success() {
        testBarber.setActive(false);

        barberService.activateBarber(testBarber);

        assertTrue(testBarber.getActive());
    }

    @Test
    void deactivateBarber_Success() {
        testBarber.setActive(true);

        barberService.deactivateBarber(testBarber);

        assertFalse(testBarber.getActive());
    }

    @Test
    void getAllLeaves_Success() {
        BarberLeave leave = new BarberLeave();
        leave.setId(1L);
        Page<BarberLeave> page = new PageImpl<>(Arrays.asList(leave));
        Pageable pageable = PageRequest.of(0, 10);

        when(barberLeaveRepository.findByBarbershopAndStatus(testBarbershop, pageable, LeaveStatus.PENDING))
                .thenReturn(page);

        Page<BarberLeave> result = barberService.getAllLeaves(testAdmin, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(barberLeaveRepository).findByBarbershopAndStatus(testBarbershop, pageable, LeaveStatus.PENDING);
    }

    @Test
    void getAllLeaves_NoAssignedBarbershop_ThrowsException() {
        testAdmin.setBarbershop(null);
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(ResourceNotFoundException.class, () -> {
            barberService.getAllLeaves(testAdmin, pageable);
        });
    }

    @Test
    void update_Success() {
        List<String> skills = Arrays.asList("Haircut", "Shave");
        List<String> workImages = Arrays.asList("image1.jpg", "image2.jpg");

        when(barberRepository.save(any())).thenReturn(testBarber);

        Barber result = barberService.update(testBarber, "New bio", skills, 5, workImages, 0.7);

        assertNotNull(result);
        assertEquals("New bio", result.getBio());
        assertEquals(skills, result.getSkills());
        assertEquals(5, result.getExperienceYears());
        assertEquals(workImages, result.getWorkImages());
        assertEquals(0.7, result.getCommissionRate());
        verify(barberRepository).save(testBarber);
    }

    @Test
    void countByBarbershop_Success() {
        when(barberRepository.countByBarbershop(testBarbershop)).thenReturn(5);

        Integer count = barberService.countByBarbershop(testBarbershop);

        assertEquals(5, count);
        verify(barberRepository).countByBarbershop(testBarbershop);
    }

    @Test
    void countByBarbershopAndAvailableTrue_Success() {
        when(barberRepository.countByBarbershopAndAvailableTrue(testBarbershop)).thenReturn(3);

        Integer count = barberService.countByBarbershopAndAvailableTrue(testBarbershop);

        assertEquals(3, count);
        verify(barberRepository).countByBarbershopAndAvailableTrue(testBarbershop);
    }

    @Test
    void findTopBarbersByShops_Success() {
        List<Barber> barbers = Arrays.asList(testBarber);
        PageRequest pageRequest = PageRequest.of(0, 5);

        when(barberRepository.findTopBarbersByBarbershop(testBarbershop, pageRequest)).thenReturn(barbers);

        List<Barber> result = barberService.findTopBarbersByShops(testBarbershop, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(barberRepository).findTopBarbersByBarbershop(testBarbershop, pageRequest);
    }

    @Test
    void saveRating_Success() {
        when(barberRepository.save(testBarber)).thenReturn(testBarber);

        barberService.saveRating(testBarber);

        verify(barberRepository).save(testBarber);
    }
}
