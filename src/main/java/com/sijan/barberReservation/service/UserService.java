package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberShop;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Roles;
import com.sijan.barberReservation.model.User;
import com.sijan.barberReservation.repository.BarberRepository;
import com.sijan.barberReservation.repository.BarberShopRepository;
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

    public Customer registerCustomer(Customer customer) {
        if (userRepository.findByEmail(customer.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }
        String rawPassword = customer.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        customer.setPassword(encodedPassword);

        return customerRepository.save(customer);
    }

    public Barber registerBarber(Barber barber, BarberShop shop) {
        if(userRepository.existsByEmail(barber.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        String rawPassword = barber.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        barber.setPassword(encodedPassword);
        barber.setBarbershop(shop);
        return barberRepository.save(barber);
    }

//    public BarberShop registerBarberShop(Admin shopAdmin, Long adminId) {
//        userRepository.findById(adminId).ifPresentOrElse(admin -> {
//            if (admin.getRole() != Roles.ADMIN) {
//                throw new RuntimeException("Only admin can register barbers");
//            }
//        }, () -> {
//            throw new RuntimeException("Admin not found");
//        });
//
//        String rawPassword = shopAdmin.getPassword();
//        String encodedPassword = passwordEncoder.encode(rawPassword);
//        shopAdmin.setPassword(encodedPassword);
//
//        return barberShopRepository.save(shopAdmin);
//    }
}
