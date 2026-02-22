package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.service.GoogleDriveService;
import com.sijan.barberReservation.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GoogleDriveService googleDriveService;
    private final UserService userService;

    public UserController(GoogleDriveService googleDriveService, UserService userService) {
        this.googleDriveService = googleDriveService;
        this.userService = userService;
    }

    @PostMapping("/{userId}/upload-profile")
    public ResponseEntity<Map<String, String>> uploadProfile(@PathVariable Long userId, @RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = googleDriveService.uploadProfilePicture(file);Map<String, String> response = new HashMap<>();
        userService.uploadProfilePicture(userService.findById(userId), fileUrl);
        response.put("url", fileUrl);
        return ResponseEntity.ok(response);
    }
}
