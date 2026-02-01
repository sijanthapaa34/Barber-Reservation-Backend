package com.sijan.barberReservation.mapper.service;

import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.model.ServiceOffering;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    public ServiceDTO toDTO(ServiceOffering service) {
        ServiceDTO dto = new ServiceDTO();
//        dto.setName(service.getName());
//        dto.setDescription(service.getDescription());
//        dto.setPrice(service.getPrice());
//        dto.setDurationMinutes(service.getDurationMinutes());
//        dto.setBarberShopId(service.getBarberShop().getId());
//        dto.setCreatedAt(service.getCreatedAt());
//        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }

    public ServiceOffering toEntity(ServiceDTO dto) {
        ServiceOffering service = new ServiceOffering();
//        service.setName(dto.getName());
//        service.setDescription(dto.getDescription());
//        service.setPrice(dto.getPrice());
//        service.setDurationMinutes(dto.getDurationMinutes());
        return service;
    }

    public List<ServiceDTO> toDTOs(List<ServiceOffering> services) {
        return services.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}