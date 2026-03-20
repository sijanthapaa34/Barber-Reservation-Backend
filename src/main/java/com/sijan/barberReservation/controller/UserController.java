package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.service.GoogleDriveService;
import com.sijan.barberReservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GoogleDriveService googleDriveService;
    private final UserService userService;

    @PostMapping("/{userId}/profile-picture")
    public ResponseEntity<Map<String, String>> uploadProfile(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {

        try {
            String fileUrl = googleDriveService.uploadUserProfilePicture(userId, file);
            userService.uploadProfilePicture(userService.findById(userId), fileUrl);
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
}