package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.model.Barber;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserRequestMapper {
    public Barber toEntity(UpdateUserRequest request) {
        Barber barber = new Barber();

        barber.setName(request.getName());
        barber.setPhone(request.getPhone());
        return barber;
    }

    public BarberDTO toDTO(Barber barber) {
        if (barber == null) {
            return null;
        }

        BarberDTO barberDTO = new BarberDTO();
        barberDTO.setName(barber.getName());
        barberDTO.setEmail(barber.getEmail());
        barberDTO.setPhone(barber.getPhone());
        barberDTO.setActive(barber.getActive());
        barberDTO.setBio(barber.getBio());
        barberDTO.setProfilePicture(barber.getProfilePicture());
        barberDTO.setRating(barber.getRating());
        barberDTO.setCreatedAt(barber.getCreatedAt());

        return barberDTO;
    }
}
