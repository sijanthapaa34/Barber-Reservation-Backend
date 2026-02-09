package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.service.ServiceNotFoundException;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.BarberShop;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceOfferingService {
    private final ServiceRepository serviceRepository;

    public ServiceOfferingService(ServiceRepository serviceRepository) {this.serviceRepository = serviceRepository;}

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

    public ServiceOffering add(BarberShop barberShop, ServiceOffering serviceOffering) {
        serviceOffering.setBarbershop(barberShop);
        return serviceRepository.save(serviceOffering);
    }

    public ServiceOffering update(Admin admin, ServiceOffering service) {
        return serviceRepository.save(service);
    }

    public void activateService(Admin admin, ServiceOffering service) {
        service.setAvailable(true);
        serviceRepository.save(service);
    }

    public void deactivateService(Admin admin, ServiceOffering service) {
        service.setAvailable(false);
        serviceRepository.save(service);
    }


    public Page<ServiceOffering> getAllByBarberShop(BarberShop barberShop, Pageable pageable) {
        return serviceRepository.findByBarberShop(barberShop, pageable);
    }
}
