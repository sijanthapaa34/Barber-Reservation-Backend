package com.sijan.barberReservation.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private Drive driveService;

    // Folder IDs
    private final String PROFILE_FOLDER_ID = "1juM6JqGpS3PvLZtjM0CjqZw6PKABzhU3";
    private final String APPLICATION_PROFILE_FOLDER_ID = "1HuvCv-VibLdlhFWz33LRwwwVlYHeOs99";
    private final String APPLICATION_DOC_FOLDER_ID = "1om-SIPQ0KGSu0thphG8vEdo3EyPPbIt7";
    private final String SHOP_IMAGES_FOLDER_ID = "1QuFjg_PaGU5pzOTA5efbMmqp7WahpW0r";

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

            System.out.println("Google Drive Service Initialized Successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Google Drive Service: " + e.getMessage());
        }
    }

    /**
     * Uploads a profile picture for an existing user.
     * Filename: user_{id}.jpg
     * Logic: Deletes old file (if exists) -> Uploads new file (Overwrite Strategy).
     */
    public String uploadUserProfilePicture(Long userId, MultipartFile multipartFile) throws Exception {
        String fileName = "user_" + userId + getFileExtension(multipartFile.getOriginalFilename());

        // 1. Delete old file if it exists (Cleanup)
        deleteFileByName(fileName, PROFILE_FOLDER_ID);

        // 2. Upload new file
        return uploadFile(multipartFile, PROFILE_FOLDER_ID, fileName);
    }
    public String uploadApplicationFile(MultipartFile multipartFile, String type, String email) throws Exception {
        String folderId;

        // 1. Determine Folder
        switch (type) {
            case "profile":
                folderId = APPLICATION_PROFILE_FOLDER_ID;
                break;
            case "doc":
                folderId = APPLICATION_DOC_FOLDER_ID;
                break;
            case "shop_image":
                folderId = SHOP_IMAGES_FOLDER_ID;
                break;
            default:
                folderId = APPLICATION_DOC_FOLDER_ID;
        }

        // 2. Construct Filename
        // Sanitize email to remove special characters that might cause issues in filenames
        String safeEmail = email.replaceAll("[^a-zA-Z0-9@.]", "_");

        // For Shop Images, add timestamp to allow multiple images
        String fileName;
        if (type.equals("shop_image")) {
            fileName = safeEmail + "_shop_" + System.currentTimeMillis() + getFileExtension(multipartFile.getOriginalFilename());
        } else {
            // For Profile and Doc, just use email and type (Overwrite strategy)
            fileName = safeEmail + "_" + type + getFileExtension(multipartFile.getOriginalFilename());
        }

        // 3. Delete old file if exists (Cleanup)
        deleteFileByName(fileName, folderId);

        // 4. Upload
        return uploadFile(multipartFile, folderId, fileName);
    }

    // --- Core Upload Logic ---
    private String uploadFile(MultipartFile multipartFile, String folderId, String fileName) throws Exception {
        if (multipartFile.isEmpty()) throw new RuntimeException("File is empty");

        // Create temp file
        java.io.File tempFile = java.io.File.createTempFile("upload-", getFileExtension(fileName));
        multipartFile.transferTo(tempFile);

        // Set metadata
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent = new FileContent(multipartFile.getContentType(), tempFile);

        // Upload
        com.google.api.services.drive.model.File uploadedFile = driveService.files()
                .create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id")
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

    // --- Helper: Delete Old File (For Profile Overwrite) ---
    private void deleteFileByName(String fileName, String folderId) throws Exception {
        // Search for file with this name in the specific folder
        String query = String.format("name = '%s' and '%s' in parents and trashed = false", fileName, folderId);

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id)")
                .execute();

        // Delete found files
        for (com.google.api.services.drive.model.File file : result.getFiles()) {
            driveService.files().delete(file.getId()).execute();
            System.out.println("Deleted old profile picture: " + file.getId());
        }
    }

    private String getFileExtension(String name) {
        if (name == null) return ".jpg";
        int lastDot = name.lastIndexOf(".");
        return (lastDot == -1) ? ".jpg" : name.substring(lastDot);
    }
}