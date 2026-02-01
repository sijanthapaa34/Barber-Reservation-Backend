package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.DTO.user.UserRegistrationRequest;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Roles;
import com.sijan.barberReservation.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer findById(Long id){
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
    public Customer findByEmail(String email){
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Customer getCustomerProfile(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Customer updateCustomerProfile(String email, UpdateUserRequest request) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());

        return customerRepository.save(customer);
    }

    public void changePassword(String mail, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        Customer customer = customerRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        customer.setPassword(encodedNewPassword);
        customerRepository.save(customer);
    }
}
