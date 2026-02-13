package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.exception.barber.BarberNotFoundException;
import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.BarberLeaveRepository;
import com.sijan.barberReservation.repository.BarberRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BarberService {

    private final BarberRepository barberRepository;
    private final BarberLeaveRepository barberLeaveRepository;
    private final PasswordEncoder passwordEncoder;


    public BarberService(BarberRepository barberRepository, BarberLeaveRepository barberLeaveRepository, PasswordEncoder passwordEncoder) {
        this.barberRepository = barberRepository;
        this.barberLeaveRepository = barberLeaveRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Barber findById(Long id) {
        return barberRepository.findById(id)
                .orElseThrow(() -> new BarberNotFoundException(id));
    }


    public Page<Barber> findByBarberShop(Admin admin, Pageable pageable) {
        Barbershop shop = admin.getBarbershop();
        if(shop == null){
            throw new ResourceNotFoundException("Admin has no assigned barbershop");
        }
        return barberRepository.findByBarbershop(shop, pageable);
    }

    public Barber updateBarberProfile(Barber barber) {
        return barberRepository.save(barber);
    }

    public void changePassword(String mail, ChangePasswordRequest request) {


        Barber barber = barberRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), barber.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        barber.setPassword(encodedNewPassword);
        barberRepository.save(barber);
    }
    public void applyForLeave(String mail, LocalDate startDate, LocalDate endDate, String reason) {

    }

    @Transactional
    public void activateBarber(Barber barber) {
        barber.setActive(true);
    }

    public void deactivateBarber(Barber barber) {
        barber.setActive(false);
    }

    public Page<BarberLeave> getAllLeaves(Admin admin, Pageable pageable) {
        Barbershop barberShop = admin.getBarbershop();
        if (barberShop == null) {
            throw new ResourceNotFoundException("Admin has no assigned barbershop");
        }
        return barberLeaveRepository.findByBarbershopAndStatus(barberShop, pageable, LeaveStatus.PENDING);
    }
}
