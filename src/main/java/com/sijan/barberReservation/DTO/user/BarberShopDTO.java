package com.sijan.barberReservation.DTO.user;

import com.sijan.barberReservation.DTO.service.ServiceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberShopDTO {

        private Long id;
        private String name;
        private String address;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private Double latitude;
        private Double longitude;
        private String fullAddress;
        private String phone;
        private String email;
        private String website;
        private boolean active;
        private Double rating;
        private Integer reviewCount;
        private String operatingHours;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<BarberDTO> barbers;
        private List<ServiceDTO> services;
        private AdminDTO admin;

        // Computed field for Google Maps URL
        @Transient
        public String getGoogleMapsUrl() {
            if (latitude != null && longitude != null) {
                return String.format("https://www.google.com/maps?q=%f,%f", latitude, longitude);
            } else if (fullAddress != null) {
                return String.format("https://www.google.com/maps/search/?api=1&query=%s",
                        fullAddress.replace(" ", "+"));
            }
            return null;
        }

        // Computed field for static map image URL
        @Transient
        public String getStaticMapUrl() {
            if (latitude != null && longitude != null) {
                return String.format(
                        "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=600x300&markers=color:red|%f,%f&key=YOUR_API_KEY",
                        latitude, longitude, latitude, longitude
                );
            }
            return null;
        }
    }

