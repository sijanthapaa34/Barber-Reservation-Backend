package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.exception.service.ServiceNotFoundException;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOfferingService {
    private final ServiceRepository serviceRepository;

    public ServiceOffering findById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

    }
    public List<ServiceOffering> findByIds(List<Long> ids) {
        return serviceRepository.findAllById(ids);
    }

    public Page<ServiceOffering> getAll(Pageable pageable) {
        return serviceRepository.findAll(pageable);
    }

    public ServiceOffering add(Barbershop barberShop, ServiceOffering serviceOffering) {
        serviceOffering.setBarbershop(barberShop);
        serviceOffering.setAvailable(true);
        return serviceRepository.save(serviceOffering);
    }

    @Transactional
    public ServiceOffering update(ServiceOffering service, int durationMinutes, Double price, List<String> serviceImages) {
        service.setDurationMinutes(durationMinutes);
        service.setPrice(price);
        service.setServiceImages(serviceImages);
        return serviceRepository.save(service);
    }

    @Transactional
    public void activateService(ServiceOffering service) {
        service.setAvailable(true);
        serviceRepository.save(service);
    }

    @Transactional
    public void deactivateService(ServiceOffering service) {
        service.setAvailable(false);
        serviceRepository.save(service);
    }


    public Page<ServiceOffering> getAllByBarbershop(Barbershop barberShop, Pageable pageable) {
        return serviceRepository.findByBarbershop(barberShop, pageable);
    }

    public List<ServiceOffering> findPopularServices(Barbershop shop, PageRequest of) {
        return serviceRepository.findPopularServices(shop,of);
    }


}
