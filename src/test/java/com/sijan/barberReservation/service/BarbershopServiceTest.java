package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.barbershop.BarbershopNotFoundException;
import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.BarbershopRepository;
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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarbershopServiceTest {

    @Mock
    private BarbershopRepository barbershopRepository;

    @InjectMocks
    private BarbershopService barbershopService;

    private Barbershop testBarbershop;
    private Admin shopAdmin;
    private Admin mainAdmin;

    @BeforeEach
    void setUp() {
        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Barbershop");
        testBarbershop.setAddress("123 Test St");
        testBarbershop.setCity("Kathmandu");
        testBarbershop.setState("Bagmati");
        testBarbershop.setPostalCode("44600");
        testBarbershop.setPhone("1234567890");
        testBarbershop.setLatitude(new BigDecimal("27.7172"));
        testBarbershop.setLongitude(new BigDecimal("85.3240"));
        testBarbershop.setOperatingHours("9:00 AM - 6:00 PM");
        testBarbershop.setRating(4.5);
        testBarbershop.setActive(true);
        testBarbershop.setBalance(BigDecimal.ZERO);

        shopAdmin = new Admin();
        shopAdmin.setId(1L);
        shopAdmin.setEmail("admin@test.com");
        shopAdmin.setRole(Roles.SHOP_ADMIN);
        shopAdmin.setBarbershop(testBarbershop);

        mainAdmin = new Admin();
        mainAdmin.setId(2L);
        mainAdmin.setEmail("main@test.com");
        mainAdmin.setRole(Roles.MAIN_ADMIN);
    }

    @Test
    void findById_Success() {
        when(barbershopRepository.findById(1L)).thenReturn(Optional.of(testBarbershop));

        Barbershop result = barbershopService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Barbershop", result.getName());
        verify(barbershopRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        when(barbershopRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BarbershopNotFoundException.class, () -> {
            barbershopService.findById(999L);
        });

        verify(barbershopRepository).findById(999L);
    }

    @Test
    void update_ByMainAdmin_Success() {
        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(testBarbershop);

        Barbershop result = barbershopService.update(testBarbershop, mainAdmin);

        assertNotNull(result);
        assertEquals(testBarbershop.getId(), result.getId());
        verify(barbershopRepository).save(testBarbershop);
    }

    @Test
    void update_ByShopAdmin_Success() {
        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(testBarbershop);

        Barbershop result = barbershopService.update(testBarbershop, shopAdmin);

        assertNotNull(result);
        assertEquals(testBarbershop.getId(), result.getId());
        verify(barbershopRepository).save(testBarbershop);
    }

    @Test
    void update_ByShopAdmin_WrongShop_ThrowsAccessDenied() {
        Barbershop differentShop = new Barbershop();
        differentShop.setId(999L);
        differentShop.setName("Different Shop");

        assertThrows(AccessDeniedException.class, () -> {
            barbershopService.update(differentShop, shopAdmin);
        });

        verify(barbershopRepository, never()).save(any());
    }

    @Test
    void update_ByShopAdmin_NoBarbershop_ThrowsAccessDenied() {
        Admin adminWithoutShop = new Admin();
        adminWithoutShop.setId(3L);
        adminWithoutShop.setRole(Roles.SHOP_ADMIN);
        adminWithoutShop.setBarbershop(null);

        assertThrows(AccessDeniedException.class, () -> {
            barbershopService.update(testBarbershop, adminWithoutShop);
        });

        verify(barbershopRepository, never()).save(any());
    }

    @Test
    void createBarbershop_Success() {
        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(testBarbershop);

        Barbershop result = barbershopService.createBarbershop(testBarbershop);

        assertNotNull(result);
        assertEquals("123 Test St, Kathmandu, Bagmati, 44600", result.getFullAddress());
        verify(barbershopRepository).save(testBarbershop);
    }

    @Test
    void findTopRated_Success() {
        List<Barbershop> shops = Arrays.asList(testBarbershop);
        Page<Barbershop> page = new PageImpl<>(shops);
        Pageable pageable = PageRequest.of(0, 10);

        when(barbershopRepository.findAll(pageable)).thenReturn(page);

        Page<Barbershop> result = barbershopService.findTopRated(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(barbershopRepository).findAll(pageable);
    }

    @Test
    void searchByWord_Success() {
        List<Barbershop> shops = Arrays.asList(testBarbershop);
        Page<Barbershop> page = new PageImpl<>(shops);
        Pageable pageable = PageRequest.of(0, 10);

        when(barbershopRepository.searchByKeyword("Test", pageable)).thenReturn(page);

        Page<Barbershop> result = barbershopService.searchByWord("Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(barbershopRepository).searchByKeyword("Test", pageable);
    }

    @Test
    void searchByWord_NoResults() {
        Page<Barbershop> emptyPage = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        when(barbershopRepository.searchByKeyword("NonExistent", pageable)).thenReturn(emptyPage);

        Page<Barbershop> result = barbershopService.searchByWord("NonExistent", pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(barbershopRepository).searchByKeyword("NonExistent", pageable);
    }

    @Test
    void findNearby_Success() {
        Double lat = 27.7172;
        Double lon = 85.3240;
        Double radiusKm = 5.0;
        Pageable pageable = PageRequest.of(0, 10);

        List<Barbershop> candidates = Arrays.asList(testBarbershop);
        when(barbershopRepository.findByLatitudeBetweenAndLongitudeBetween(
                any(BigDecimal.class), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(candidates);

        Page<Barbershop> result = barbershopService.findNearby(lat, lon, radiusKm, pageable);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        verify(barbershopRepository).findByLatitudeBetweenAndLongitudeBetween(
                any(BigDecimal.class), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void findNearby_NullCoordinates_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class, () -> {
            barbershopService.findNearby(null, null, 5.0, pageable);
        });

        verify(barbershopRepository, never()).findByLatitudeBetweenAndLongitudeBetween(
                any(), any(), any(), any());
    }

    @Test
    void findNearby_DefaultRadius() {
        Double lat = 27.7172;
        Double lon = 85.3240;
        Pageable pageable = PageRequest.of(0, 10);

        List<Barbershop> candidates = Arrays.asList(testBarbershop);
        when(barbershopRepository.findByLatitudeBetweenAndLongitudeBetween(
                any(BigDecimal.class), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(candidates);

        Page<Barbershop> result = barbershopService.findNearby(lat, lon, null, pageable);

        assertNotNull(result);
        verify(barbershopRepository).findByLatitudeBetweenAndLongitudeBetween(
                any(BigDecimal.class), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void findNearby_NegativeRadius_UsesDefault() {
        Double lat = 27.7172;
        Double lon = 85.3240;
        Double radiusKm = -10.0;
        Pageable pageable = PageRequest.of(0, 10);

        List<Barbershop> candidates = Arrays.asList(testBarbershop);
        when(barbershopRepository.findByLatitudeBetweenAndLongitudeBetween(
                any(BigDecimal.class), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(candidates);

        Page<Barbershop> result = barbershopService.findNearby(lat, lon, radiusKm, pageable);

        assertNotNull(result);
        verify(barbershopRepository).findByLatitudeBetweenAndLongitudeBetween(
                any(BigDecimal.class), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void distributeEarnings_Success() {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setAmount(new BigDecimal("100.00"));

        Appointment appointment = new Appointment();
        appointment.setBarbershop(testBarbershop);

        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(testBarbershop);

        barbershopService.distributeEarnings(transaction, appointment);

        assertEquals(new BigDecimal("5.00"), transaction.getPlatformFee());
        assertEquals(new BigDecimal("95.00"), transaction.getShopEarnings());
        verify(barbershopRepository).save(testBarbershop);
    }

    @Test
    void getAll_Success() {
        List<Barbershop> shops = Arrays.asList(testBarbershop);
        Page<Barbershop> page = new PageImpl<>(shops);
        Pageable pageable = PageRequest.of(0, 10);

        when(barbershopRepository.findAll(pageable)).thenReturn(page);

        Page<Barbershop> result = barbershopService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(barbershopRepository).findAll(pageable);
    }

    @Test
    void countByActiveTrue_Success() {
        when(barbershopRepository.countByActiveTrue()).thenReturn(5L);

        long count = barbershopService.countByActiveTrue();

        assertEquals(5L, count);
        verify(barbershopRepository).countByActiveTrue();
    }

    @Test
    void findTop4ByActiveTrueOrderByRatingDesc_Success() {
        List<Barbershop> topShops = Arrays.asList(testBarbershop);
        when(barbershopRepository.findTop4ByActiveTrueOrderByRatingDesc()).thenReturn(topShops);

        List<Barbershop> result = barbershopService.findTop4ByActiveTrueOrderByRatingDesc();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(barbershopRepository).findTop4ByActiveTrueOrderByRatingDesc();
    }

    @Test
    void saveRating_Success() {
        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(testBarbershop);

        barbershopService.saveRating(testBarbershop);

        verify(barbershopRepository).save(testBarbershop);
    }
}
