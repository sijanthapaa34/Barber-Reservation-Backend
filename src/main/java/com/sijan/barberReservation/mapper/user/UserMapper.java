package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.user.UserDTO;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setActive(user.getActive());
        dto.setRole(user.getRole());
        dto.setProfilePicture(user.getProfilePicture());
        if (user instanceof Admin) {
            Admin admin = (Admin) user;
            if (admin.getBarbershop() != null) {
                dto.setShopId(admin.getBarbershop().getId());
            }
        }
        return dto;
    }
}
