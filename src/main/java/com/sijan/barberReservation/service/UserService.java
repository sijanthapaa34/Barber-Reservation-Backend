package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.BarberRepository;
import com.sijan.barberReservation.repository.CustomerRepository;
import com.sijan.barberReservation.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public Barber registerBarber(Barber barber, Barbershop shop) {
        if(userRepository.existsByEmail(barber.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }
        barber.setRole(Roles.BARBER);
        String rawPassword = barber.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        barber.setPassword(encodedPassword);
        barber.setBarbershop(shop);
        return barberRepository.save(barber);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User id not found"));
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
