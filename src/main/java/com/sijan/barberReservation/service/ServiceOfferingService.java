package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.service.RegisterServiceRequest;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.repository.ServiceRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceOfferingService {
    private final ServiceRepository serviceRepository;

    public ServiceOfferingService(ServiceRepository serviceRepository) {this.serviceRepository = serviceRepository;}

    public ServiceOffering findById(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

    }
    public List<ServiceOffering> findByIds(List<Long> ids) {
        return serviceRepository.findAllById(ids);
    }

    public List<ServiceOffering> getAllServices(Long adminId, Long barbershopId) {
        return serviceRepository.findAll();
    }

    public ServiceOffering register(Long adminId, Long barbershopId, RegisterServiceRequest request) {
        return new ServiceOffering();
    }
}
