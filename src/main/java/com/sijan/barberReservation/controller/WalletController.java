package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.model.User;
import com.sijan.barberReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final PaymentService paymentService;

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@AuthenticationPrincipal User user) {
        BigDecimal balance = paymentService.getMyBalance(user);

        Map<String, Object> res = new HashMap<>();
        res.put("role", user.getClass().getSimpleName());
        res.put("balance", balance);

        return ResponseEntity.ok(res);
    }
}