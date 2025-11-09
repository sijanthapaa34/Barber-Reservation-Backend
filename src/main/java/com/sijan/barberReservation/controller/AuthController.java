package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.user.AuthRequest;
import com.sijan.barberReservation.DTO.user.RegisterBarberRequest;
import com.sijan.barberReservation.DTO.user.RegisterCustomerRequest;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.User;
import com.sijan.barberReservation.security.JwtTokenProvider;
import com.sijan.barberReservation.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider tokenProvider,
                          UserService userService) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request){
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userService.findByEmail(request.getEmail())
                ;

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(response);
    }
    @PostMapping("/customer")
    public Customer registerCustomer(@RequestBody RegisterCustomerRequest req) {
        return userService.registerCustomer(req);
    }

    @PostMapping("/barber/{adminId}")
    public Barber registerBarber(@PathVariable Long adminId,
                                 @RequestBody RegisterBarberRequest req) {
        return userService.registerBarber(req, adminId);
    }
}
