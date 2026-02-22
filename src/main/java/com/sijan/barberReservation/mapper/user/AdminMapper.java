package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.Auth.RegisterBarbershopRequest;
import com.sijan.barberReservation.model.Admin;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {
    public Admin toEntity(RegisterBarbershopRequest request) {
        Admin admin = new Admin();
        admin.setName(request.getAdminName());
        admin.setEmail(request.getAdminEmail());
        admin.setPhone(request.getPhone());
        return admin;
    }
}
