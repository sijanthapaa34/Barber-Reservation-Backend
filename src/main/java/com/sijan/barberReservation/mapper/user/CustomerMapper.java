package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.DTO.Auth.RegisterCustomerRequest;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Roles;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomerMapper {
    public CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setRole(customer.getRole() != null ? customer.getRole().name() : null);
        dto.setPoints(customer.getPoints());
        dto.setTotalBookings(customer.getTotalBookings());
        dto.setStatus(customer.getStatus());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setLastBookingAt(customer.getLastVisited());
        dto.setProfilePicture(customer.getProfilePicture());
        return dto;
    }

    public Customer toEntity(RegisterCustomerRequest req) {

        Customer customer = new Customer();
        customer.setName(req.getName());
        customer.setEmail(req.getEmail());
        customer.setPhone(req.getPhone());
        customer.setPassword(req.getPassword());
        customer.setRole(Roles.CUSTOMER);
        customer.setProfilePicture(req.getProfilePictureUrl());
        customer.setCreatedAt(LocalDateTime.now());

        return customer;
    }
}
