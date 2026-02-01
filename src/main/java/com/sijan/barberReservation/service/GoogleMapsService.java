//package com.sijan.barberReservation.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//@Service
//public class GoogleMapsService {
//
//    @Value("${google.maps.api.key}")
//    private String apiKey;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    /**
//     * Get latitude and longitude from address
//     */
//    public LocationCoordinates geocodeAddress(String address) {
//        String url = String.format(
//                "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
//                address.replace(" ", "+"), apiKey
//        );
//
//        try {
//            String response = restTemplate.getForObject(url, String.class);
//            JsonNode root = objectMapper.readTree(response);
//
//            if (root.path("status").asText().equals("OK") &&
//                    root.path("results").size() > 0) {
//
//                JsonNode location = root.path("results").get(0)
//                        .path("geometry").path("location");
//
//                Double lat = location.path("lat").asDouble();
//                Double lng = location.path("lng").asDouble();
//
//                return new LocationCoordinates(lat, lng);
//            }
//        } catch (Exception e) {
//            // Log error
//        }
//
//        return null;
//    }
//
//    /**
//     * Get formatted address from coordinates
//     */
//    public String reverseGeocode(Double latitude, Double longitude) {
//        String url = String.format(
//                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
//                latitude, longitude, apiKey
//        );
//
//        try {
//            String response = restTemplate.getForObject(url, String.class);
//            JsonNode root = objectMapper.readTree(response);
//
//            if (root.path("status").asText().equals("OK") &&
//                    root.path("results").size() > 0) {
//
//                return root.path("results").get(0)
//                        .path("formatted_address").asText();
//            }
//        } catch (Exception e) {
//            // Log error
//        }
//
//        return null;
//    }
//
//    /**
//     * Get nearby barbershops
//     */
//    public String findNearbyPlaces(Double latitude, Double longitude, String type) {
//        String url = String.format(
//                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=5000&type=%s&key=%s",
//                latitude, longitude, type, apiKey
//        );
//
//        return restTemplate.getForObject(url, String.class);
//    }
//
//    public static class LocationCoordinates {
//        private Double latitude;
//        private Double longitude;
//
//        public LocationCoordinates(Double latitude, Double longitude) {
//            this.latitude = latitude;
//            this.longitude = longitude;
//        }
//
//        // Getters
//        public Double getLatitude() { return latitude; }
//        public Double getLongitude() { return longitude; }
//    }
//}