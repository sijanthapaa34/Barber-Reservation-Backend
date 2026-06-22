package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.Auth.RegisterBarberRequest;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Barbershop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BarberMapperTest {

    private BarberMapper barberMapper;

    @BeforeEach
    void setUp() {
        barberMapper = new BarberMapper();
    }

    @Test
    void toDTO_WithCompleteBarber_ShouldMapAllFields() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);
        shop.setName("Test Barbershop");

        Barber barber = new Barber();
        barber.setId(1L);
        barber.setName("John Barber");
        barber.setActive(true);
        barber.setEmail("john@barber.com");
        barber.setWorkImages(Arrays.asList("img1.jpg", "img2.jpg"));
        barber.setPhone("1234567890");
        barber.setBarbershop(shop);
        barber.setBio("Experienced barber");
        barber.setProfilePicture("profile.jpg");
        barber.setRating(4.5);
        barber.setExperienceYears(5);
        barber.setAvailable(true);
        barber.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));
        barber.setCommissionRate(0.3);

        // Act
        BarberDTO dto = barberMapper.toDTO(barber);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Barber", dto.getName());
        assertTrue(dto.isActive());
        assertEquals("john@barber.com", dto.getEmail());
        assertEquals(Arrays.asList("img1.jpg", "img2.jpg"), dto.getWorkImages());
        assertEquals("1234567890", dto.getPhone());
        assertEquals("Test Barbershop", dto.getBarbershop());
        assertEquals("Experienced barber", dto.getBio());
        assertEquals("profile.jpg", dto.getProfilePicture());
        assertEquals(4.5, dto.getRating());
        assertEquals(5, dto.getExperienceYears());
        assertTrue(dto.getAvailable());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), dto.getCreatedAt());
        assertEquals(0.3, dto.getCommissionRate());
    }

    @Test
    void toDTO_WithNullActiveField_ShouldDefaultToTrue() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setName("Shop");

        Barber barber = new Barber();
        barber.setId(2L);
        barber.setName("Jane Barber");
        barber.setActive(null);
        barber.setBarbershop(shop);

        // Act
        BarberDTO dto = barberMapper.toDTO(barber);

        // Assert
        assertTrue(dto.isActive());
    }

    @Test
    void toDTO_WithNullRating_ShouldDefaultToZero() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setName("Shop");

        Barber barber = new Barber();
        barber.setId(3L);
        barber.setName("Bob Barber");
        barber.setRating(null);
        barber.setBarbershop(shop);

        // Act
        BarberDTO dto = barberMapper.toDTO(barber);

        // Assert
        assertEquals(0.0, dto.getRating());
    }

    @Test
    void toDTO_WithNullAvailable_ShouldDefaultToTrue() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setName("Shop");

        Barber barber = new Barber();
        barber.setId(4L);
        barber.setName("Alice Barber");
        barber.setAvailable(null);
        barber.setBarbershop(shop);

        // Act
        BarberDTO dto = barberMapper.toDTO(barber);

        // Assert
        assertTrue(dto.getAvailable());
    }

    @Test
    void toEntity_WithCompleteRequest_ShouldMapAllFields() {
        // Arrange
        RegisterBarberRequest request = new RegisterBarberRequest();
        request.setName("New Barber");
        request.setEmail("new@barber.com");
        request.setPhone("9876543210");
        request.setWorkImages(Arrays.asList("work1.jpg", "work2.jpg"));
        request.setPassword("password123");
        request.setBio("New to the shop");
        request.setExperienceYears(3);
        request.setCommissionRate(0.25);

        // Act
        Barber barber = barberMapper.toEntity(request);

        // Assert
        assertNotNull(barber);
        assertEquals("New Barber", barber.getName());
        assertEquals("new@barber.com", barber.getEmail());
        assertEquals("9876543210", barber.getPhone());
        assertEquals(Arrays.asList("work1.jpg", "work2.jpg"), barber.getWorkImages());
        assertEquals("password123", barber.getPassword());
        assertEquals("New to the shop", barber.getBio());
        assertEquals(3, barber.getExperienceYears());
        assertNotNull(barber.getCreatedAt());
        assertEquals(0.25, barber.getCommissionRate());
    }

    @Test
    void toEntity_ShouldSetCreatedAtToNow() {
        // Arrange
        RegisterBarberRequest request = new RegisterBarberRequest();
        request.setName("Test Barber");
        request.setEmail("test@barber.com");
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // Act
        Barber barber = barberMapper.toEntity(request);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertNotNull(barber.getCreatedAt());
        assertTrue(barber.getCreatedAt().isAfter(before));
        assertTrue(barber.getCreatedAt().isBefore(after));
    }

    @Test
    void toDTOs_WithMultipleBarbers_ShouldMapAll() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setName("Shop");

        Barber barber1 = new Barber();
        barber1.setId(1L);
        barber1.setName("Barber 1");
        barber1.setBarbershop(shop);

        Barber barber2 = new Barber();
        barber2.setId(2L);
        barber2.setName("Barber 2");
        barber2.setBarbershop(shop);

        List<Barber> barbers = Arrays.asList(barber1, barber2);

        // Act
        List<BarberDTO> dtos = barberMapper.toDTOs(barbers);

        // Assert
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Barber 1", dtos.get(0).getName());
        assertEquals("Barber 2", dtos.get(1).getName());
    }

    @Test
    void toDTOs_WithEmptyList_ShouldReturnEmptyList() {
        // Act
        List<BarberDTO> dtos = barberMapper.toDTOs(Arrays.asList());

        // Assert
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}
