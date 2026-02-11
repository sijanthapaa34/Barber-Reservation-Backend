package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.repository.BarbershopRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class BarbershopService {

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

//    public BarberShop createBarbershop(Long adminId, BarberShop barbershop) {
//        Admin admin = adminService.findById(adminId);
//
//        return barbershopRepository.save(barbershop);
//    }

    /**
     * Find barbershops near a location
     */
//    public List<BarberShopDTO> findNearbyBarbershops(Double latitude, Double longitude, Double radiusKm) {
//        // Calculate bounding box for the search
//        double latDelta = radiusKm / 111.0; // Approximate km per degree latitude
//        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));
//
//        double minLat = latitude - latDelta;
//        double maxLat = latitude + latDelta;
//        double minLon = longitude - lonDelta;
//        double maxLon = longitude + lonDelta;
//
//        List<BarberShop> barbershops = barbershopRepository.findByLocationBounds(
//                minLat, maxLat, minLon, maxLon);
//
//        return barbershops.stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }

    public Barbershop updateBarbershop(Long adminId, Long id, Barbershop barberShop) {
        return barbershopRepository.save(barberShop);
    }

    public Barbershop createBarbershopWithAdmin(Barbershop barbershop, Admin admin) {
        Barbershop savedBarbershop = barbershopRepository.save(barbershop);

        // Link the admin to the saved barbershop
        admin.setBarbershop(savedBarbershop);

        // Save the admin
        adminService.register(admin);

        return savedBarbershop;
    }
}