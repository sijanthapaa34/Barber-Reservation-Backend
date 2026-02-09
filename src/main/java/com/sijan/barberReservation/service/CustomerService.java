package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.exception.auth.InvalidPasswordException;
import com.sijan.barberReservation.exception.customer.CustomerNotFoundException;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional
    public Customer update(Customer customer, String name, String phone) {
        customer.setName(name);
        customer.setPhone(phone);
        return customer;
    }

    @Transactional
    public void changePassword(
            Customer customer,
            String currentPassword,
            String newPassword
    ) {
        if (!passwordEncoder.matches(currentPassword, customer.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
    }

    public Page<Customer> getFrequentCustomers(Admin admin, Pageable pageable) {
        return customerRepository.findFrequentCustomersByAdmin(admin, pageable);
    }
}
