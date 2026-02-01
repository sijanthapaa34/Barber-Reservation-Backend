package com.sijan.barberReservation.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
@Service
public class GoogleTokenVerifierService {

    @Value("${google.clientId.web}")
    private String webClientId;

    public GoogleIdToken.Payload verifyToken(String idToken) throws Exception {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(webClientId))
                .build();

        GoogleIdToken token = verifier.verify(idToken);
        if (token == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }

        return token.getPayload();
    }
}
