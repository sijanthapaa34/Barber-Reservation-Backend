package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.Auth.RegisterBarbershopRequest;
import com.sijan.barberReservation.DTO.google.GoogleLoginRequest;
import com.sijan.barberReservation.DTO.user.AdminDTO;
import com.sijan.barberReservation.DTO.Auth.AuthRequest;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.DTO.Auth.RegisterBarberRequest;
import com.sijan.barberReservation.DTO.Auth.RegisterCustomerRequest;
import com.sijan.barberReservation.mapper.user.*;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.security.JwtTokenProvider;
import com.sijan.barberReservation.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final BarbershopService barbershopService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final BarberMapper barberMapper;
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final BarbershopMapper barbershopMapper;
    private final CustomerMapper customerMapper;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest){
        System.out.println(request.getPassword());
        String token = authService.login(request, httpRequest);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(response);
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, String>> loginWithGoogle(
            @RequestBody GoogleLoginRequest request, HttpServletRequest httpRequest
    ) throws Exception {

        String token = authService.loginWithGoogle(request, httpRequest);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(response);
    }

    @PostMapping("/customer")
    public ResponseEntity<Map<String, String>> registerCustomer(@RequestBody RegisterCustomerRequest req) {
        boolean isValid = otpService.verifyOtp(req.getEmail(), req.getOtp());
        if (!isValid) {
            throw new RuntimeException("Invalid or expired OTP.");
        }
        if (userService.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email is already registered.");
        }

        Customer newCustomer = customerMapper.toEntity(req);
        Customer registeredCustomer = userService.registerCustomer(newCustomer);

        emailService.sendRegistrationConfirmation(registeredCustomer.getEmail(), registeredCustomer.getName());
        String token = tokenProvider.generateToken(registeredCustomer.getEmail(), registeredCustomer.getId(), registeredCustomer.getRole().toString());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(response);
    }

    @PostMapping("/barber/{barbershopId}")
    public ResponseEntity<BarberDTO> registerBarber(@PathVariable Long barbershopId,
                                    @RequestBody RegisterBarberRequest req) {
        Barber barber = barberMapper.toEntity(req);
        Barbershop shop = barbershopService.findById(barbershopId);
        // Register Barber
        Barber registeredBarber = userService.registerBarber(barber, shop);
        // Send Email Notification to the Barber
        emailService.sendRegistrationConfirmation(registeredBarber.getEmail(), registeredBarber.getName());

        return ResponseEntity.ok(barberMapper.toDTO(registeredBarber));
    }
    @PostMapping("/barbershop")
    public ResponseEntity<AdminDTO> registerBarberShop(
            @RequestBody RegisterBarbershopRequest request) {

        Barbershop barbershop = barbershopMapper.toEntity(request);
        Admin admin = adminMapper.toEntity(request);
        Barbershop savedBarbershop = barbershopService.createBarbershop(barbershop);
        Admin savedAdmin = userService.registerAdmin(admin, savedBarbershop);
        return ResponseEntity.status(201).body(adminMapper.toDTO(savedAdmin));
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            System.out.println("error null principal");
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        User user = userService.findById(userPrincipal.getId());
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        // Get user and generate new token
        User user = userService.findById(userPrincipal.getId());
        String newToken = tokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().toString()
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("token", newToken);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + newToken)
                .body(response);
    }
}
