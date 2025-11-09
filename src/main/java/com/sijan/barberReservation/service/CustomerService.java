package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.DTO.user.UserRegistrationRequest;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Roles;
import com.sijan.barberReservation.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    public CustomerDTO getCustomerProfile(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName(customer.getName());
        customerDTO.setEmail(customer.getEmail());
        customerDTO.setPhone(customer.getPhone());
        customerDTO.setPoints(customer.getPoints());
        customerDTO.setCreatedAt(customer.getCreatedAt());

        return customerDTO;
    }

    public CustomerDTO updateCustomerProfile(String email, UpdateUserRequest request) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());

        customerRepository.save(customer);

        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName(customer.getName());
        customerDTO.setEmail(customer.getEmail());
        customerDTO.setPhone(customer.getPhone());
        customerDTO.setPoints(customer.getPoints());
        customerDTO.setCreatedAt(customer.getCreatedAt());

        return customerDTO;
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // In real system, compare encoded password using encoder.matches()
        // For now, assuming raw password check (not secure — just example)
        if (!customer.getPassword().equals(request.getCurrentPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Encode and save new password
//        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
//        customer.setPassword(encodedPassword);
        customerRepository.save(customer);
    }
}
