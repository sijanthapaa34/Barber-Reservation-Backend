package com.sijan.barberReservation.mapper.service;

import com.sijan.barberReservation.DTO.service.RegisterServiceRequest;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.model.ServiceOffering;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    public ServiceDTO toDTO(ServiceOffering service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(dto.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setDurationMinutes(service.getDurationMinutes());
        dto.setBarberShop(service.getBarbershop().getName());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }

    public ServiceOffering toEntity(RegisterServiceRequest dto) {
        ServiceOffering service = new ServiceOffering();
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setDurationMinutes(dto.getDurationMinutes());
        service.setPrice(dto.getPrice());
        service.setAvailable(dto.getAvailable());
        service.setCategory(dto.getCategory());

        service.setServiceImage(dto.getServiceImage());
        service.setServiceImages(
                dto.getServiceImages() != null
                        ? new ArrayList<>(dto.getServiceImages())
                        : new ArrayList<>()
        );

        service.setTargetGender(dto.getTargetGender());

        return service;
    }

    public List<ServiceDTO> toDTOs(List<ServiceOffering> services) {
        return services.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}