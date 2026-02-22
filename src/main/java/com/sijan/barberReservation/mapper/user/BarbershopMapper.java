package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.Auth.RegisterBarbershopRequest;
import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.UpdateBarbershopRequest;
import com.sijan.barberReservation.model.Barbershop;
import org.springframework.stereotype.Component;

@Component
public class BarbershopMapper {
    public BarbershopDTO toDTO(Barbershop shop) {
        BarbershopDTO dto = new BarbershopDTO();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setAddress(shop.getAddress());
        dto.setCity(shop.getCity());
        dto.setState(shop.getState());
        dto.setPostalCode(shop.getPostalCode());
        dto.setPhone(shop.getPhone());
        dto.setEmail(shop.getEmail());
        dto.setProfilePicture(shop.getProfilePicture());
        dto.setWebsite(shop.getWebsite());
        dto.setOperatingHours(shop.getOperatingHours());
        dto.setRating(shop.getRating());
        return dto;
    }


    public Barbershop toEntity(RegisterBarbershopRequest req) {
        Barbershop shop = new Barbershop();
        shop.setName(req.getShopName());
        shop.setAddress(req.getAddress());
        shop.setCity(req.getCity());
        shop.setPostalCode(req.getPostalCode());
        shop.setPhone(req.getPhone());
        shop.setEmail(req.getShopEmail());
        shop.setLongitude(req.getLongitude());
        shop.setLatitude(req.getLatitude());
        shop.setWebsite(req.getWebsite());
        shop.setOperatingHours(req.getOperatingHours());

        return shop;
    }

    public Barbershop toEntity(UpdateBarbershopRequest req) {
        Barbershop shop = new Barbershop();
        shop.setName(req.getName());
        shop.setAddress(req.getAddress());
        shop.setCity(req.getCity());
        shop.setPhone(req.getPhone());
        shop.setEmail(req.getEmail());
        return shop;
    }
}
