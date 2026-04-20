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

import java.security.SecureRandom;
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
    private final EmailService emailService;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a secure random password
     */
    private String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
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
        
        // Generate random password if not provided
        String rawPassword = barber.getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = generateRandomPassword(10);
        }
        
        String encodedPassword = passwordEncoder.encode(rawPassword);
        barber.setPassword(encodedPassword);
        barber.setBarbershop(shop);
        Barber savedBarber = barberRepository.save(barber);
        
        // Send credentials email to the barber
        emailService.sendBarberCredentials(
            savedBarber.getEmail(), 
            savedBarber.getName(), 
            shop.getName(), 
            rawPassword
        );
        
        return savedBarber;
    }

    public Barber registerBarberOfApplication(Barber barber, Barbershop shop) {
        if(userRepository.existsByEmail(barber.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }
        barber.setRole(Roles.BARBER);
        barber.setBarbershop(shop);
        return barberRepository.save(barber);
    }
    public Admin registerAdmin(Admin admin, Barbershop shop) {
        if(userRepository.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }
        admin.setRole(Roles.SHOP_ADMIN);
        
        // Generate random password if not provided
        String rawPassword = admin.getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = generateRandomPassword(10);
        }
        
        String encodedPassword = passwordEncoder.encode(rawPassword);
        admin.setPassword(encodedPassword);
        admin.setBarbershop(shop);
        Admin savedAdmin = adminRepository.save(admin);
        
        // Send credentials email to the shop admin
        emailService.sendShopAdminCredentials(
            savedAdmin.getEmail(), 
            savedAdmin.getName(), 
            shop.getName(), 
            rawPassword
        );
        
        return savedAdmin;
    }
    public Admin registerAdminOfApplication(Admin admin, Barbershop shop) {
        if(userRepository.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }
        admin.setRole(Roles.SHOP_ADMIN);
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

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
