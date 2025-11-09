package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.RegisterBarberRequest;
import com.sijan.barberReservation.DTO.user.RegisterCustomerRequest;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Roles;
import com.sijan.barberReservation.model.User;
import com.sijan.barberReservation.repository.BarberRepository;
import com.sijan.barberReservation.repository.CustomerRepository;
import com.sijan.barberReservation.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final BarberRepository barberRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       BarberRepository barberRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.barberRepository = barberRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Customer registerCustomer(RegisterCustomerRequest req) {
        if (userRepository.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }
        String encodedPassword = passwordEncoder.encode(req.getPassword());

        Customer customer = new Customer();
        customer.setName(req.getName());
        customer.setEmail(req.getEmail());
        customer.setPhone(req.getPhone());
        customer.setPassword(encodedPassword);
        customer.setRole(Roles.CUSTOMER);
        customer.setPreferences(req.getPreferences());
        customer.setCreatedAt(LocalDateTime.now());

        return customerRepository.save(customer);
    }

    public Barber registerBarber(RegisterBarberRequest req, Long adminId) {
        userRepository.findById(adminId).ifPresentOrElse(admin -> {
            if (admin.getRole() != Roles.ADMIN) {
                throw new RuntimeException("Only admin can register barbers");
            }
        }, () -> {
            throw new RuntimeException("Admin not found");
        });

        userRepository.findByEmail(req.getEmail());

        Barber barber = new Barber();
        barber.setName(req.getName());
        barber.setEmail(req.getEmail());
        barber.setPhone(req.getPhone());
        barber.setPassword(req.getPassword());
        barber.setBio(req.getBio());
        barber.setExperienceYears(req.getExperienceYears());
        barber.setRole(Roles.BARBER);
        barber.setCreatedAt(LocalDateTime.now());

        return barberRepository.save(barber);
    }
}
