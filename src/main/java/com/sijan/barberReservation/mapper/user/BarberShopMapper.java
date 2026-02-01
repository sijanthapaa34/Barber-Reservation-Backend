package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.user.BarberShopDTO;
import com.sijan.barberReservation.DTO.user.RegisterBarberShopRequest;
import com.sijan.barberReservation.DTO.user.UpdateBarberShopRequest;
import com.sijan.barberReservation.model.BarberShop;
import org.springframework.stereotype.Component;

@Component
public class BarberShopMapper {
    public BarberShopDTO toDTO(BarberShop shop) {
        BarberShopDTO dto = new BarberShopDTO();
        dto.setName(shop.getName());
        dto.setAddress(shop.getAddress());
        dto.setCity(shop.getCity());
        dto.setPhone(shop.getPhone());
        dto.setEmail(shop.getEmail());
        dto.setActive(shop.isActive());
        return dto;
    }

    public BarberShop toEntity(RegisterBarberShopRequest req) {
        BarberShop shop = new BarberShop();
        shop.setName(req.getName());
        shop.setAddress(req.getAddress());
        shop.setCity(req.getCity());
        shop.setPhone(req.getPhone());
        shop.setEmail(req.getEmail());
        return shop;
    }

    public BarberShop toEntity(UpdateBarberShopRequest req) {
        BarberShop shop = new BarberShop();
        shop.setName(req.getName());
        shop.setAddress(req.getAddress());
        shop.setCity(req.getCity());
        shop.setPhone(req.getPhone());
        shop.setEmail(req.getEmail());
        return shop;
    }
}
