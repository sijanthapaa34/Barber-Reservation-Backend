package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.RegisterBarberRequest;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Roles;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BarberMapper {

    public BarberDTO toDTO(Barber barber) {
        BarberDTO dto = new BarberDTO();
        dto.setName(barber.getName());
        dto.setActive(barber.getActive() != null ? barber.getActive() : true);
        dto.setEmail(barber.getEmail());
        dto.setPhone(barber.getPhone());
        dto.setBio(barber.getBio());
        dto.setProfilePictureUrl(barber.getProfilePicture());
        dto.setRating(barber.getRating() != null ? barber.getRating() : 0.0);
        dto.setExperienceYears(barber.getExperienceYears());
        dto.setAvailable(barber.getAvailable() != null ? barber.getAvailable() : true);
//        dto.setBarberShopId(barber.getBarberShop() != null ? barber.getBarberShop().getId() : null);
        dto.setCreatedAt(barber.getCreatedAt());
        return dto;
    }

    public Barber toEntity(RegisterBarberRequest req){
        Barber barber = new Barber();
        barber.setName(req.getName());
        barber.setEmail(req.getEmail());
        barber.setPhone(req.getPhone());
        barber.setPassword(req.getPassword());
        barber.setBio(req.getBio());
        barber.setExperienceYears(req.getExperienceYears());
        barber.setRole(Roles.BARBER);
        barber.setCreatedAt(LocalDateTime.now());
        return barber;
    }

    public List<BarberDTO> toDTOs(List<Barber> barbers) {
        return barbers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}