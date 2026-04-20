package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.Auth.RegisterBarbershopRequest;
import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.UpdateBarbershopRequest;
import com.sijan.barberReservation.model.Barbershop;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BarbershopMapper {
    public BarbershopDTO toDTO(Barbershop shop) {
        BarbershopDTO dto = new BarbershopDTO();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setDescription(shop.getDescription());
        dto.setAddress(shop.getAddress());
        dto.setCity(shop.getCity());
        dto.setShopImages(shop.getShopImages());
        dto.setState(shop.getState());
        dto.setPostalCode(shop.getPostalCode());
        dto.setPhone(shop.getPhone());
        dto.setLatitude(shop.getLatitude());
        dto.setLongitude(shop.getLongitude());
        dto.setEmail(shop.getEmail());
        dto.setProfilePicture(shop.getProfilePicture());
        dto.setWebsite(shop.getWebsite());
        dto.setOperatingHours(shop.getOperatingHours());
        dto.setRating(shop.getRating());
        // Add admin ID for chat functionality
        if (shop.getAdmin() != null) {
            dto.setAdminId(shop.getAdmin().getId());
        }
        return dto;
    }


    public Barbershop toEntity(RegisterBarbershopRequest req) {
        Barbershop shop = new Barbershop();
        shop.setName(req.getShopName());
        shop.setDescription(req.getDescription());
        shop.setState(req.getState());
        shop.setAddress(req.getAddress());
        shop.setCity(req.getCity());
        shop.setShopImages(req.getShopImages());
        shop.setPostalCode(req.getPostalCode());
        shop.setPhone(req.getPhone());
        shop.setEmail(req.getShopEmail());
        shop.setLongitude(req.getLongitude());
        shop.setLatitude(req.getLatitude());
        shop.setWebsite(req.getWebsite());
        shop.setOperatingHours(req.getOperatingHours());

        return shop;
    }

    public List<BarbershopDTO> toDTOs(List<Barbershop> shops) {
        return shops.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
