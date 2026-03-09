package com.sijan.barberReservation.mapper.application;

import com.sijan.barberReservation.DTO.application.ApplicationDetailResponse;
import com.sijan.barberReservation.DTO.application.ApplicationRequest;
import com.sijan.barberReservation.model.Application;
import com.sijan.barberReservation.model.ApplicationStatus;
import com.sijan.barberReservation.model.ApplicationType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class ApplicationMapper {

    public ApplicationDetailResponse toDTO(Application app) {
        ApplicationDetailResponse dto = new ApplicationDetailResponse();

        dto.setId(app.getId());
        dto.setType(app.getType().name());
        dto.setStatus(app.getStatus().name());

        // Common
        dto.setName(app.getName());
        dto.setEmail(app.getEmail());
        dto.setPhone(app.getPhone());
        dto.setCreatedAt(app.getCreatedAt());

        // Skills
        if (app.getSkills() != null && !app.getSkills().isEmpty()) {
            dto.setSkills(Arrays.stream(app.getSkills().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        } else {
            dto.setSkills(Collections.emptyList());
        }

        // Barber
        dto.setExperienceYears(app.getExperienceYears());
        dto.setBio(app.getBio());
        dto.setCity(app.getCity());
        dto.setProfilePictureUrl(app.getProfilePictureUrl());
        dto.setLicenseUrl(app.getLicenseUrl());

        // Shop
        dto.setShopName(app.getShopName());
        dto.setAddress(app.getAddress());
        dto.setState(app.getState());
        dto.setPostalCode(app.getPostalCode());

        // Direct assignment (BigDecimal to BigDecimal)
        dto.setLatitude(app.getLatitude());
        dto.setLongitude(app.getLongitude());

        dto.setWebsite(app.getWebsite());
        dto.setOperatingHours(app.getOperatingHours());
        dto.setDescription(app.getDescription());
        dto.setDocumentUrl(app.getDocumentUrl());

        // Review
        dto.setReviewedBy(app.getReviewedBy());
        dto.setReviewedAt(app.getReviewedAt());
        dto.setRejectionReason(app.getRejectionReason());

        return dto;
    }

    public Application toEntity(ApplicationRequest req) {
        Application app = new Application();

        try {
            app.setType(ApplicationType.valueOf(req.getType().toUpperCase()));
        } catch (Exception e) {
            app.setType(ApplicationType.BARBER);
        }

        app.setStatus(ApplicationStatus.PENDING);

        // Common
        app.setName(req.getName());
        app.setEmail(req.getEmail());
        app.setPhone(req.getPhone());
        app.setPassword(req.getPassword());

        // Barber
        app.setExperienceYears(req.getExperienceYears());
        app.setBio(req.getBio());
        app.setCity(req.getCity());
        app.setProfilePictureUrl(req.getProfilePictureUrl());
        app.setLicenseUrl(req.getLicenseUrl());

        // Skills List -> String
        if (req.getSkills() != null && !req.getSkills().isEmpty()) {
            app.setSkills(String.join(",", req.getSkills()));
        }

        // Shop
        app.setShopName(req.getShopName());
        app.setAddress(req.getAddress());
        app.setState(req.getState());
        app.setPostalCode(req.getPostalCode());

        // Direct assignment (BigDecimal to BigDecimal)
        app.setLatitude(req.getLatitude());
        app.setLongitude(req.getLongitude());

        app.setWebsite(req.getWebsite());
        app.setOperatingHours(req.getOperatingHours());
        app.setDescription(req.getDescription());
        app.setDocumentUrl(req.getDocumentUrl());

        return app;
    }
}