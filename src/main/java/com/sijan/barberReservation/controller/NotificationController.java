package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/token")
    public ResponseEntity<Void> saveToken(
            @RequestBody String token,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String userType = userPrincipal.getAuthorities().iterator().next().getAuthority(); // e.g. "ROLE_CUSTOMER"
        notificationService.saveToken(userPrincipal.getId(), token, userType.replace("ROLE_", ""));
        return ResponseEntity.ok().build();
    }
}