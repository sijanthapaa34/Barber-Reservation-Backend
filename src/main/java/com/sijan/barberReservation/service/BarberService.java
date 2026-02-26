package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.exception.auth.InvalidPasswordException;
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
import java.util.List;

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


    public Page<Barber> findByBarberShop(Barbershop shop, Pageable pageable) {
        return barberRepository.findByBarbershop(shop, pageable);
    }

    @Transactional
    public void changePassword(Barber barber, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, barber.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        barber.setPassword(passwordEncoder.encode(newPassword));
    }
    public void applyForLeave(String mail, LocalDate startDate, LocalDate endDate, String reason) {

    }

    @Transactional
    public void activateBarber(Barber barber) {
        barber.setActive(true);
    }
    @Transactional
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

    @Transactional
    public Barber update(Barber barber, String name, String phone, String bio, List<String> skills, Integer experienceYears) {
        barber.setName(name);
        barber.setPhone(phone);
        barber.setBio(bio);
        barber.setSkills(skills);
        barber.setExperienceYears(experienceYears);
        return barber;
    }
}
