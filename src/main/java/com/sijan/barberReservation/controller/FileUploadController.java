package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {
    private final GoogleDriveService googleDriveService;

    @PostMapping
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "doc") String type,
            @RequestParam("email") String email // Added email parameter
    ) {
        try {
            // Pass email to service
            String url = googleDriveService.uploadApplicationFile(file, type, email);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed");
        }
    }
}