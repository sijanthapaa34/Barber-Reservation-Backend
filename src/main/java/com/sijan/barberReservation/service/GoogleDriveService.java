package com.sijan.barberReservation.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class GoogleDriveService {

    private Drive driveService;

    private String folderId = "1juM6JqGpS3PvLZtjM0CjqZw6PKABzhU3";

    @PostConstruct
    public void init() {
        try {
            InputStream credentialsStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("service-account.json");

            if (credentialsStream == null) {
                throw new RuntimeException("Credentials file 'service-account.json' not found in classpath!");
            }

            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(credentialsStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/drive"));

            driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            )
                    .setApplicationName("BarberReservationApp")
                    .build();

            System.out.println("Google Drive Service Initialized Successfully for folder: " + folderId);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Google Drive Service: " + e.getMessage());
        }
    }

    public String uploadProfilePicture(MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String originalName = multipartFile.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + extension;

        // Create temp file
        java.io.File tempFile = java.io.File.createTempFile("upload-", extension);
        multipartFile.transferTo(tempFile);

        // Set metadata
        File fileMetadata = new File();
        fileMetadata.setName(newFileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent = new FileContent(multipartFile.getContentType(), tempFile);

        // Upload
        // FIX 2: supportsAllDrives(true) ensures this works for both regular folders and Shared Drives
        File uploadedFile = driveService.files()
                .create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id, parents")
                .execute();

        // Set Permission to Public
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");

        driveService.permissions()
                .create(uploadedFile.getId(), permission)
                .setSupportsAllDrives(true)
                .execute();

        // Delete temp file
        if (tempFile.exists()) {
            tempFile.delete();
        }

        return "https://drive.google.com/uc?id=" + uploadedFile.getId();
    }
}