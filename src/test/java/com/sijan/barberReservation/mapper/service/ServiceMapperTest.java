package com.sijan.barberReservation.mapper.service;

import com.sijan.barberReservation.DTO.service.RegisterServiceRequest;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.ServiceCategory;
import com.sijan.barberReservation.model.ServiceOffering;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceMapperTest {

    private ServiceMapper serviceMapper;

    @BeforeEach
    void setUp() {
        serviceMapper = new ServiceMapper();
    }

    @Test
    void toDTO_WithCompleteService_ShouldMapAllFields() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);
        shop.setName("Test Shop");

        ServiceOffering service = new ServiceOffering();
        service.setId(1L);
        service.setName("Haircut");
        service.setDescription("Professional haircut");
        service.setPrice(25.0);
        service.setDurationMinutes(30);
        service.setBarbershop(shop);
        service.setServiceImages(Arrays.asList("img1.jpg", "img2.jpg"));
        service.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));
        service.setAvailable(true);
        service.setCategory(ServiceCategory.HAIRCUT);
        service.setTargetGender("MALE");
        service.setUpdatedAt(LocalDateTime.of(2023, 2, 1, 10, 0));

        // Act
        ServiceDTO dto = serviceMapper.toDTO(service);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Haircut", dto.getName());
        assertEquals("Professional haircut", dto.getDescription());
        assertEquals(25.0, dto.getPrice());
        assertEquals(30, dto.getDurationMinutes());
        assertEquals(1L, dto.getBarbershopId());
        assertEquals("Test Shop", dto.getBarbershop());
        assertEquals(Arrays.asList("img1.jpg", "img2.jpg"), dto.getServiceImages());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), dto.getCreatedAt());
        assertTrue(dto.getAvailable());
        assertEquals("HAIRCUT", dto.getCategory());
        assertEquals("MALE", dto.getTargetGender());
        assertEquals(LocalDateTime.of(2023, 2, 1, 10, 0), dto.getUpdatedAt());
    }

    @Test
    void toEntity_WithCompleteRequest_ShouldMapAllFields() {
        // Arrange
        RegisterServiceRequest request = new RegisterServiceRequest();
        request.setName("Beard Trim");
        request.setDescription("Professional beard trim");
        request.setDurationMinutes(20);
        request.setPrice(15.0);
        request.setAvailable(true);
        request.setCategory(ServiceCategory.BEARD);
        request.setServiceImages(Arrays.asList("beard1.jpg", "beard2.jpg"));
        request.setTargetGender("MALE");

        // Act
        ServiceOffering service = serviceMapper.toEntity(request);

        // Assert
        assertNotNull(service);
        assertEquals("Beard Trim", service.getName());
        assertEquals("Professional beard trim", service.getDescription());
        assertEquals(20, service.getDurationMinutes());
        assertEquals(15.0, service.getPrice());
        assertTrue(service.getAvailable());
        assertEquals(ServiceCategory.BEARD, service.getCategory());
        assertEquals(Arrays.asList("beard1.jpg", "beard2.jpg"), service.getServiceImages());
        assertEquals("MALE", service.getTargetGender());
    }

    @Test
    void toEntity_WithNullServiceImages_ShouldCreateEmptyList() {
        // Arrange
        RegisterServiceRequest request = new RegisterServiceRequest();
        request.setName("Facial");
        request.setDescription("Clean facial");
        request.setDurationMinutes(15);
        request.setPrice(10.0);
        request.setAvailable(true);
        request.setCategory(ServiceCategory.FACIAL);
        request.setServiceImages(null);
        request.setTargetGender("MALE");

        // Act
        ServiceOffering service = serviceMapper.toEntity(request);

        // Assert
        assertNotNull(service);
        assertNotNull(service.getServiceImages());
        assertTrue(service.getServiceImages().isEmpty());
    }

    @Test
    void toEntity_WithEmptyServiceImages_ShouldCreateEmptyList() {
        // Arrange
        RegisterServiceRequest request = new RegisterServiceRequest();
        request.setName("Hair Color");
        request.setDescription("Hair coloring");
        request.setDurationMinutes(60);
        request.setPrice(50.0);
        request.setAvailable(true);
        request.setCategory(ServiceCategory.COLOR);
        request.setServiceImages(Arrays.asList());
        request.setTargetGender("UNISEX");

        // Act
        ServiceOffering service = serviceMapper.toEntity(request);

        // Assert
        assertNotNull(service);
        assertNotNull(service.getServiceImages());
        assertTrue(service.getServiceImages().isEmpty());
    }

    @Test
    void toDTOs_WithMultipleServices_ShouldMapAll() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);
        shop.setName("Shop");

        ServiceOffering service1 = new ServiceOffering();
        service1.setId(1L);
        service1.setName("Service 1");
        service1.setBarbershop(shop);
        service1.setCategory(ServiceCategory.HAIRCUT);

        ServiceOffering service2 = new ServiceOffering();
        service2.setId(2L);
        service2.setName("Service 2");
        service2.setBarbershop(shop);
        service2.setCategory(ServiceCategory.BEARD);

        List<ServiceOffering> services = Arrays.asList(service1, service2);

        // Act
        List<ServiceDTO> dtos = serviceMapper.toDTOs(services);

        // Assert
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Service 1", dtos.get(0).getName());
        assertEquals("Service 2", dtos.get(1).getName());
    }

    @Test
    void toDTOs_WithEmptyList_ShouldReturnEmptyList() {
        // Act
        List<ServiceDTO> dtos = serviceMapper.toDTOs(Arrays.asList());

        // Assert
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void toDTO_WithUnavailableService_ShouldMapAvailableAsFalse() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);
        shop.setName("Shop");

        ServiceOffering service = new ServiceOffering();
        service.setId(1L);
        service.setName("Unavailable Service");
        service.setBarbershop(shop);
        service.setAvailable(false);
        service.setCategory(ServiceCategory.HAIRCUT);

        // Act
        ServiceDTO dto = serviceMapper.toDTO(service);

        // Assert
        assertFalse(dto.getAvailable());
    }
}
