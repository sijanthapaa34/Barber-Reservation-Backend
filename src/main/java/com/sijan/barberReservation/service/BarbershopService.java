package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.UpdateBarberShopRequest;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.BarberShop;
import com.sijan.barberReservation.repository.BarberShopRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class BarbershopService {

    private final BarberShopRepository barbershopRepository;
    private final AdminService adminService;
//    private final GoogleMapsService googleMapsService;

    public BarbershopService(BarberShopRepository barbershopRepository,
                             AdminService adminService
//                             GoogleMapsService googleMapsService
    ) {
        this.barbershopRepository = barbershopRepository;
        this.adminService = adminService;
//        this.googleMapsService = googleMapsService;
    }

    public BarberShop findById(Long id){
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

    public BarberShop updateBarbershop(Long adminId, Long id,BarberShop barberShop) {
        return barbershopRepository.save(barberShop);
    }

    public BarberShop createBarbershopWithAdmin(BarberShop barbershop, Admin admin) {
        BarberShop savedBarbershop = barbershopRepository.save(barbershop);

        // Link the admin to the saved barbershop
        admin.setBarbershop(savedBarbershop);

        // Save the admin
        adminService.register(admin);

        return savedBarbershop;
    }
}