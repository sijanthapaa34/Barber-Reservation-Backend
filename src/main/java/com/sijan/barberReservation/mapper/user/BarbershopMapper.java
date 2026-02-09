package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.Auth.RegisterBarbershopRequest;
import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.UpdateBarbershopRequest;
import com.sijan.barberReservation.model.BarberShop;
import org.springframework.stereotype.Component;

@Component
public class BarbershopMapper {
    public BarbershopDTO toDTO(BarberShop shop) {
        BarbershopDTO dto = new BarbershopDTO();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setAddress(shop.getAddress());
        dto.setCity(shop.getCity());
        dto.setState(shop.getState());
        dto.setPostalCode(shop.getPostalCode());
        dto.setPhone(shop.getPhone());
        dto.setEmail(shop.getEmail());
        dto.setWebsite(shop.getWebsite());
        dto.setOperatingHours(shop.getOperatingHours());
        dto.setRating(shop.getRating());
        return dto;
    }


    public BarberShop toEntity(RegisterBarbershopRequest req) {
        BarberShop shop = new BarberShop();
        shop.setName(req.getName());
        shop.setAddress(req.getAddress());
        shop.setCity(req.getCity());
        shop.setPhone(req.getPhone());
        shop.setEmail(req.getEmail());
        return shop;
    }

    public BarberShop toEntity(UpdateBarbershopRequest req) {
        BarberShop shop = new BarberShop();
        shop.setName(req.getName());
        shop.setAddress(req.getAddress());
        shop.setCity(req.getCity());
        shop.setPhone(req.getPhone());
        shop.setEmail(req.getEmail());
        return shop;
    }
}
