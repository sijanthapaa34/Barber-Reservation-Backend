package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.Auth.RegisterBarbershopRequest;
import com.sijan.barberReservation.DTO.user.AdminDTO;
import com.sijan.barberReservation.model.Admin;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public Admin toEntity(RegisterBarbershopRequest request) {
        Admin admin = new Admin();
        admin.setName(request.getAdminName());
        admin.setEmail(request.getAdminEmail());
        admin.setPassword(request.getPassword());
        admin.setProfilePicture(request.getAdminProfile());
        admin.setPhone(request.getPhone());
        return admin;
    }

    public AdminDTO toDTO(Admin updated) {
        AdminDTO adminDTO = new AdminDTO(
                updated.getId(),
                updated.getName(),
                updated.getEmail(),
                updated.getPreferredContactMethod(), // maps to phone
                updated.getAdminLevel(),
                updated.getProfileImage(),
                updated.getBarbershop() != null ? updated.getBarbershop().getId() : null,
                updated.getBarbershop() != null ? updated.getBarbershop().getName() : null
        );
        return adminDTO;
    }
}
