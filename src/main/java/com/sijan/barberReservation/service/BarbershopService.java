package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.AdminLevel;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.Roles;
import com.sijan.barberReservation.repository.BarbershopRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class BarbershopService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final BarbershopRepository barbershopRepository;
    private final AdminService adminService;
//    private final GoogleMapsService googleMapsService;

    public BarbershopService(BarbershopRepository barbershopRepository,
                             AdminService adminService
//                             GoogleMapsService googleMapsService
    ) {
        this.barbershopRepository = barbershopRepository;
        this.adminService = adminService;
//        this.googleMapsService = googleMapsService;
    }

    public Barbershop findById(Long id){
        return barbershopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barbershop not found"));
    }

    public Barbershop update(Barbershop barberShop, Admin admin) {
        if (admin.getRole() == Roles.MAIN_ADMIN) {
            return barbershopRepository.save(barberShop);
        }

        if (admin.getBarbershop() == null ||
                !Objects.equals(admin.getBarbershop().getId(), barberShop.getId())) {
            throw new AccessDeniedException("Not authorized for this shop");
        }
        return barbershopRepository.save(barberShop);
    }

    public Barbershop createBarbershopWithAdmin(Barbershop barbershop, Admin admin) {
        barbershop.setAdmin(admin);
        String fullAddress = String.format("%s, %s, %s, %s",
                barbershop.getAddress(), barbershop.getCity(),
                barbershop.getState(), barbershop.getPostalCode());
        barbershop.setFullAddress(fullAddress);

        Barbershop savedBarbershop = barbershopRepository.save(barbershop);

        admin.setRole(Roles.SHOP_ADMIN);
        admin.setBarbershop(savedBarbershop);
        adminService.register(admin);
        return savedBarbershop;
    }

    public Page<Barbershop> findTopRated(Pageable pageable) {
        return barbershopRepository.findAll(pageable);
    }

    public Page<Barbershop> searchByWord(String word, Pageable pageable) {
        return barbershopRepository.searchByKeyword(word, pageable);
    }

    public Page<Barbershop> findNearby(Double lat, Double lon, Double radiusKm, Pageable pageable) {

        // 1. Validation
        if (lat == null || lon == null) throw new IllegalArgumentException("Coordinates required");

        // 2. Defaults
        if (radiusKm == null || radiusKm <= 0) radiusKm = 5.0;

        // 3. LOGIC: Calculate Bounding Box (The Square)
        // Approx 1 degree = 111km. We calculate a rough box to fetch candidates.
        double latDelta = radiusKm / 111.0;
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        BigDecimal latMin = BigDecimal.valueOf(lat - latDelta);
        BigDecimal latMax = BigDecimal.valueOf(lat + latDelta);
        BigDecimal lonMin = BigDecimal.valueOf(lon - lonDelta);
        BigDecimal lonMax = BigDecimal.valueOf(lon + lonDelta);

        // 4. Fetch Candidates (SQL does simple filtering here)
        List<Barbershop> candidates = barbershopRepository
                .findByLatitudeBetweenAndLongitudeBetween(latMin, latMax, lonMin, lonMax);

        // 5. MATH: Filter & Sort using Haversine Formula in Java
        BigDecimal userLat = BigDecimal.valueOf(lat);
        BigDecimal userLon = BigDecimal.valueOf(lon);

        Double finalRadiusKm = radiusKm;
        List<Barbershop> filteredShops = candidates.stream()
                // Filter by exact distance
                .filter(shop -> {
                    double distance = calculateHaversineDistance(
                            userLat.doubleValue(),
                            userLon.doubleValue(),
                            shop.getLatitude().doubleValue(),
                            shop.getLongitude().doubleValue()
                    );
                    return distance <= finalRadiusKm;
                })
                // Sort by distance (closest first)
                .sorted((s1, s2) -> {
                    double d1 = calculateHaversineDistance(userLat.doubleValue(), userLon.doubleValue(), s1.getLatitude().doubleValue(), s1.getLongitude().doubleValue());
                    double d2 = calculateHaversineDistance(userLat.doubleValue(), userLon.doubleValue(), s2.getLatitude().doubleValue(), s2.getLongitude().doubleValue());
                    return Double.compare(d1, d2);
                })
                .collect(Collectors.toList());

        // 6. Manual Pagination (Since we filtered in Java, we handle paging manually)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredShops.size());

        List<Barbershop> pageContent = filteredShops.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredShops.size());
    }

    // --- The Math Helper (Haversine Formula) ---
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}