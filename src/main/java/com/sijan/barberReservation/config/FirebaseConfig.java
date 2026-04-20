package com.sijan.barberReservation.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

   @PostConstruct
    public void initialize() {
        try {
            System.out.println("Initializing Firebase...");
            
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (!resource.exists()) {
                throw new RuntimeException("firebase-service-account.json not found");
            }
            
            GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId("fadebooknotification")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully");
                System.out.println("Project: fadebooknotification");
            }
        } catch (IOException e) {
            System.err.println("Firebase init failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}