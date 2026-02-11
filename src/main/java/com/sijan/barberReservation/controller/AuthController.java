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
import com.sijan.barberReservation.mapper.user.BarberMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final BarbershopService barbershopService;
    private final PasswordEncoder passwordEncoder;
    private final BarberMapper barberMapper;
    private final CustomerMapper customerMapper;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider tokenProvider,
                          UserService userService, BarbershopService barbershopService, PasswordEncoder passwordEncoder, BarberMapper barberMapper, CustomerMapper customerMapper, GoogleTokenVerifierService googleTokenVerifierService) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.barbershopService = barbershopService;
        this.passwordEncoder = passwordEncoder;
        this.barberMapper = barberMapper;
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
        response.put("email", email);
        response.put("name", name);
        response.put("picture", picture);

        return ResponseEntity.ok(response);
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
    public ResponseEntity<CustomerDTO> registerCustomer(@RequestBody RegisterCustomerRequest req) {
        Customer customer = customerMapper.toEntity(req);
        CustomerDTO response = customerMapper.toDTO(userService.registerCustomer(customer));
        return ResponseEntity.ok(response);
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
            @RequestBody RegisterBarbershopRequest req) {

        // Create the barbershop
        Barbershop barbershop = new Barbershop();
        barbershop.setName(req.getName());
        barbershop.setAddress(req.getAddress());
        barbershop.setCity(req.getCity());
        barbershop.setState(req.getState());
        barbershop.setPostalCode(req.getPostalCode());
        barbershop.setCountry(req.getCountry());
        barbershop.setLatitude(req.getLatitude());
        barbershop.setLongitude(req.getLongitude());
        barbershop.setPhone(req.getPhone());
        barbershop.setEmail(req.getEmail());
        barbershop.setWebsite(req.getWebsite());
        barbershop.setOperatingHours(req.getOperatingHours());

        // Create full address string
        String fullAddress = String.format("%s, %s, %s, %s, %s",
                req.getAddress(), req.getCity(),
                req.getState(), req.getPostalCode(), req.getCountry());
        barbershop.setFullAddress(fullAddress);

        // Create the admin (shop owner)
        Admin admin = new Admin();
        admin.setName(req.getName());
        admin.setEmail(req.getEmail());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setPhone(req.getPhone());
        admin.setRole(Roles.SHOP_ADMIN);
        admin.setAdminLevel(AdminLevel.SHOP_ADMIN);
        admin.setBarbershop(barbershop);

        barbershop.setAdmin(admin);
        // Save both entities
        Barbershop savedBarbershop = barbershopService.createBarbershopWithAdmin(barbershop, admin);

        // Create response
        BarbershopDTO response = new BarbershopDTO();
        return ResponseEntity.status(201).body(response);
    }

    private AdminDTO createAdminDTO(Admin admin) {

        AdminDTO dto = new AdminDTO();
        dto.setId(admin.getId());
        dto.setName(admin.getName());
        dto.setEmail(admin.getEmail());
        dto.setPhone(admin.getPhone());
        dto.setProfileImage(admin.getProfileImage());
        dto.setAdminLevel(admin.getAdminLevel());
        dto.setBarbershopId(admin.getBarbershop().getId());
        dto.setBarbershopName(admin.getBarbershop().getName());
        return dto;
    }



}
