package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.user.UpdateBarbershopRequest;
import com.sijan.barberReservation.model.Barbershop;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UpdateBarbershopRequestMapper {
    public Barbershop toEntity(Barbershop shop, UpdateBarbershopRequest request) {
        Optional.ofNullable(request.getName()).ifPresent(shop::setName);
        Optional.ofNullable(request.getAddress()).ifPresent(shop::setAddress);
        Optional.ofNullable(request.getCity()).ifPresent(shop::setCity);
        Optional.ofNullable(request.getState()).ifPresent(shop::setState);
        Optional.ofNullable(request.getPostalCode()).ifPresent(shop::setPostalCode);
        Optional.ofNullable(request.getCountry()).ifPresent(shop::setCountry);

        Optional.ofNullable(request.getLatitude()).ifPresent(shop::setLatitude);
        Optional.ofNullable(request.getLongitude()).ifPresent(shop::setLongitude);

        Optional.ofNullable(request.getPhone()).ifPresent(shop::setPhone);
        Optional.ofNullable(request.getEmail()).ifPresent(shop::setEmail);
        Optional.ofNullable(request.getWebsite()).ifPresent(shop::setWebsite);
        Optional.ofNullable(request.getOperatingHours()).ifPresent(shop::setOperatingHours);

        return shop;
    }
}
