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
import com.sijan.barberReservation.mapper.user.AdminMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.security.JwtTokenProvider;
import com.sijan.barberReservation.service.BarbershopService;
import com.sijan.barberReservation.service.GoogleTokenVerifierService;
import com.sijan.barberReservation.service.UserService;
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
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final BarbershopService barbershopService;
    private final PasswordEncoder passwordEncoder;
    private final BarberMapper barberMapper;
    private final AdminMapper adminMapper;
    private final BarbershopMapper barbershopMapper;
    private final CustomerMapper customerMapper;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider tokenProvider,
                          UserService userService, BarbershopService barbershopService, PasswordEncoder passwordEncoder, BarberMapper barberMapper, AdminMapper adminMapper, BarbershopMapper barbershopMapper, CustomerMapper customerMapper, GoogleTokenVerifierService googleTokenVerifierService) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.barbershopService = barbershopService;
        this.passwordEncoder = passwordEncoder;
        this.barberMapper = barberMapper;
        this.adminMapper = adminMapper;
        this.barbershopMapper = barbershopMapper;
        this.customerMapper = customerMapper;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request){
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userService.findByEmail(request.getEmail());

        String token = tokenProvider.generateToken(user.getEmail(),user.getId() ,user.getRole().toString());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(response);
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, String>> loginWithGoogle(
            @RequestBody GoogleLoginRequest request
    ) throws Exception {

        var payload = googleTokenVerifierService.verifyToken(request.getIdToken());

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = userService.findByEmail(email);
        if (user == null) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setName(name);
            customer.setProfilePicture(picture);
            customer.setRole(Roles.CUSTOMER);
            String randomPassword = UUID.randomUUID().toString();
            customer.setPassword(passwordEncoder.encode(randomPassword));
            user = userService.registerCustomer(customer);
        }

        String token = tokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().toString()
        );

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(response);
    }


//    @PostMapping("/facebook")
//    public ResponseEntity<Map<String, String>> loginWithFacebook(
//            @RequestParam String idToken,
//            @RequestParam String platform) throws Exception {
//
//        // Verify Facebook token and get user info
//        var userInfo = facebookTokenVerifierService.verifyToken(idToken);
//
//        String email = userInfo.getEmail();
//        String name = userInfo.getName();
//        String picture = userInfo.getPicture();
//
//        // Check if user exists
//        User user = userService.findByEmail(email);
//        if (user == null) {
//            // Create a new customer
//            Customer customer = new Customer();
//            customer.setEmail(email);
//            customer.setName(name);
//            customer.setRole(Roles.CUSTOMER);
//            user = userService.registerCustomer(customer);
//        }
//
//        // Generate JWT
//        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());
//
//        Map<String, String> response = new HashMap<>();
//        response.put("token", token);
//        response.put("email", email);
//        response.put("name", name);
//        response.put("picture", picture);
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/customer")
    public ResponseEntity<Map<String, String>> registerCustomer(@RequestBody RegisterCustomerRequest req) {
        Customer newCustomer = customerMapper.toEntity(req);
        Customer registeredcustomer = userService.registerCustomer(newCustomer);

        String token = tokenProvider.generateToken(registeredcustomer.getEmail(),registeredcustomer.getId() ,registeredcustomer.getRole().toString());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(response);
    }

    @PostMapping("/barber/{barberShopId}")
    public ResponseEntity<BarberDTO> registerBarber(@PathVariable Long barberShopId,
                                    @RequestBody RegisterBarberRequest req) {
        Barber barber = barberMapper.toEntity(req);
        Barbershop shop = barbershopService.findById(barberShopId);
        BarberDTO response = barberMapper.toDTO(userService.registerBarber(barber, shop));
        return ResponseEntity.ok(response);
    }
    @PostMapping("/barbershop")
    public ResponseEntity<BarbershopDTO> registerBarberShop(
            @RequestBody RegisterBarbershopRequest request) {

        Barbershop barbershop = barbershopMapper.toEntity(request);
        Admin admin = adminMapper.toEntity(request);
        Barbershop savedBarbershop = barbershopService.createBarbershopWithAdmin(barbershop, admin);
        return ResponseEntity.status(201).body(barbershopMapper.toDTO(savedBarbershop));
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // Defensive check
        if (userPrincipal == null) {
            System.out.println("error null principal");
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        User user = userService.findById(userPrincipal.getId());
        return ResponseEntity.ok(user);
    }
}
