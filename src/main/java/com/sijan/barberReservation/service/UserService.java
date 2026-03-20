package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AdminRepository;
import com.sijan.barberReservation.repository.BarberRepository;
import com.sijan.barberReservation.repository.CustomerRepository;
import com.sijan.barberReservation.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final BarberRepository barberRepository;
    private final PasswordEncoder passwordEncoder;

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
    public Admin registerAdmin(Admin admin, Barbershop shop) {
        if(userRepository.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }
        admin.setRole(Roles.SHOP_ADMIN);
        String rawPassword = admin.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        admin.setPassword(encodedPassword);
        admin.setBarbershop(shop);
        return adminRepository.save(admin);
    }




    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User id not found"));
    }

    @Transactional
    public void uploadProfilePicture(User user, String fileUrl) {
        user.setProfilePicture(fileUrl);
    }

    public User registerGoogleCustomer(String email, String name, String picture) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setName(name);
        customer.setProfilePicture(picture);
        customer.setRole(Roles.CUSTOMER);
        String randomPassword = UUID.randomUUID().toString();
        customer.setPassword(passwordEncoder.encode(randomPassword));
        return customerRepository.save(customer);
    }

    public long count() {
        return userRepository.count();
    }

    public int countByLastLoginAfter(LocalDateTime localDateTime) {
        return userRepository.countByLastLoginAfter(localDateTime);
    }
}
