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
    private final String folderId = "https://drive.google.com/drive/folders/1BQrYoLbt-HNbQWUqPWN-5-F5RyS1OFM7?usp=drive_link";

    @PostConstruct
    public void init() throws Exception {

        InputStream credentialsStream = getClass()
                .getClassLoader()
                .getResourceAsStream("service-account.json");

        if (credentialsStream == null) {
            throw new RuntimeException("service-account.json not found!");
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
    }

    public String uploadProfilePicture(MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String originalName = multipartFile.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + extension;

        java.io.File tempFile = java.io.File.createTempFile("upload-", extension);
        multipartFile.transferTo(tempFile);

        File fileMetadata = new File();
        fileMetadata.setName(newFileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent =
                new FileContent(multipartFile.getContentType(), tempFile);

        File uploadedFile = driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        // Make public
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");

        driveService.permissions()
                .create(uploadedFile.getId(), permission)
                .execute();

        tempFile.delete();

        return "https://drive.google.com/uc?id=" + uploadedFile.getId();
    }
}