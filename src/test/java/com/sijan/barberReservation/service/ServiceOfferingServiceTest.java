package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.service.ServiceNotFoundException;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.repository.AdminRepository;
import com.sijan.barberReservation.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOfferingServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ServiceOfferingService serviceOfferingService;

    @Test
    void findById_Success() {
        // Arrange
        Long serviceId = 1L;
        ServiceOffering service = new ServiceOffering();
        service.setId(serviceId);
        service.setName("Haircut");

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        // Act
        ServiceOffering result = serviceOfferingService.findById(serviceId);

        // Assert
        assertNotNull(result);
        assertEquals(serviceId, result.getId());
        assertEquals("Haircut", result.getName());
        verify(serviceRepository, times(1)).findById(serviceId);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long serviceId = 999L;
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceNotFoundException.class, () -> {
            serviceOfferingService.findById(serviceId);
        });
        verify(serviceRepository, times(1)).findById(serviceId);
    }

    @Test
    void findByIds_Success() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        ServiceOffering service1 = new ServiceOffering();
        service1.setId(1L);
        ServiceOffering service2 = new ServiceOffering();
        service2.setId(2L);
        ServiceOffering service3 = new ServiceOffering();
        service3.setId(3L);

        when(serviceRepository.findAllById(ids)).thenReturn(Arrays.asList(service1, service2, service3));

        // Act
        List<ServiceOffering> result = serviceOfferingService.findByIds(ids);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(serviceRepository, times(1)).findAllById(ids);
    }

    @Test
    void getAll_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        ServiceOffering service1 = new ServiceOffering();
        ServiceOffering service2 = new ServiceOffering();
        Page<ServiceOffering> page = new PageImpl<>(Arrays.asList(service1, service2));

        when(serviceRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<ServiceOffering> result = serviceOfferingService.getAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(serviceRepository, times(1)).findAll(pageable);
    }

    @Test
    void add_Success() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);
        shop.setName("Test Shop");

        ServiceOffering service = new ServiceOffering();
        service.setName("Haircut");
        service.setPrice(500.0);

        ServiceOffering savedService = new ServiceOffering();
        savedService.setId(1L);
        savedService.setName("Haircut");
        savedService.setPrice(500.0);
        savedService.setBarbershop(shop);
        savedService.setAvailable(true);

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setBarbershop(shop);

        when(serviceRepository.save(any(ServiceOffering.class))).thenReturn(savedService);
        when(adminRepository.findByBarbershop(shop)).thenReturn(Optional.of(admin));

        // Act
        ServiceOffering result = serviceOfferingService.add(shop, service);

        // Assert
        assertNotNull(result);
        assertEquals(shop, result.getBarbershop());
        assertTrue(result.getAvailable());
        verify(serviceRepository, times(1)).save(any(ServiceOffering.class));
        verify(notificationService, times(1)).sendServiceAddedToShopAdmin(admin.getId(), "Haircut");
    }

    @Test
    void add_WithoutAdmin() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);

        ServiceOffering service = new ServiceOffering();
        service.setName("Haircut");

        ServiceOffering savedService = new ServiceOffering();
        savedService.setId(1L);
        savedService.setBarbershop(shop);
        savedService.setAvailable(true);

        when(serviceRepository.save(any(ServiceOffering.class))).thenReturn(savedService);
        when(adminRepository.findByBarbershop(shop)).thenReturn(Optional.empty());

        // Act
        ServiceOffering result = serviceOfferingService.add(shop, service);

        // Assert
        assertNotNull(result);
        verify(serviceRepository, times(1)).save(any(ServiceOffering.class));
        verify(notificationService, never()).sendServiceAddedToShopAdmin(anyLong(), anyString());
    }

    @Test
    void update_Success() {
        // Arrange
        ServiceOffering service = new ServiceOffering();
        service.setId(1L);
        service.setName("Haircut");

        int durationMinutes = 30;
        Double price = 600.0;
        List<String> serviceImages = Arrays.asList("image1.jpg", "image2.jpg");

        when(serviceRepository.save(service)).thenReturn(service);

        // Act
        ServiceOffering result = serviceOfferingService.update(service, durationMinutes, price, serviceImages);

        // Assert
        assertNotNull(result);
        assertEquals(durationMinutes, result.getDurationMinutes());
        assertEquals(price, result.getPrice());
        assertEquals(serviceImages, result.getServiceImages());
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void activateService_Success() {
        // Arrange
        ServiceOffering service = new ServiceOffering();
        service.setId(1L);
        service.setAvailable(false);

        when(serviceRepository.save(service)).thenReturn(service);

        // Act
        serviceOfferingService.activateService(service);

        // Assert
        assertTrue(service.getAvailable());
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void deactivateService_Success() {
        // Arrange
        ServiceOffering service = new ServiceOffering();
        service.setId(1L);
        service.setAvailable(true);

        when(serviceRepository.save(service)).thenReturn(service);

        // Act
        serviceOfferingService.deactivateService(service);

        // Assert
        assertFalse(service.getAvailable());
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void getAllByBarbershop_Success() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        ServiceOffering service1 = new ServiceOffering();
        ServiceOffering service2 = new ServiceOffering();
        Page<ServiceOffering> page = new PageImpl<>(Arrays.asList(service1, service2));

        when(serviceRepository.findByBarbershop(shop, pageable)).thenReturn(page);

        // Act
        Page<ServiceOffering> result = serviceOfferingService.getAllByBarbershop(shop, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(serviceRepository, times(1)).findByBarbershop(shop, pageable);
    }

    @Test
    void findPopularServices_Success() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);

        PageRequest pageRequest = PageRequest.of(0, 5);
        ServiceOffering service1 = new ServiceOffering();
        ServiceOffering service2 = new ServiceOffering();
        List<ServiceOffering> services = Arrays.asList(service1, service2);

        when(serviceRepository.findPopularServices(shop, pageRequest)).thenReturn(services);

        // Act
        List<ServiceOffering> result = serviceOfferingService.findPopularServices(shop, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(serviceRepository, times(1)).findPopularServices(shop, pageRequest);
    }
}
