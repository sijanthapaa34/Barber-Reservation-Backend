package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.RegisterBarberRequest;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberShop;
import com.sijan.barberReservation.repository.BarberLeaveRepository;
import com.sijan.barberReservation.repository.BarberRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class BarberService {

    private final BarberRepository barberRepository;
    private final PasswordEncoder passwordEncoder;
    private final BarberShopService barberShopService;
    private final BarberLeaveRepository barberLeaveRepository;


    public BarberService(BarberRepository barberRepository, PasswordEncoder passwordEncoder, BarberShopService barberShopService, BarberLeaveRepository barberLeaveRepository) {
        this.barberRepository = barberRepository;
        this.passwordEncoder = passwordEncoder;
        this.barberShopService = barberShopService;
        this.barberLeaveRepository = barberLeaveRepository;
    }

    public Barber findById(Long id) {
        return barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barber not found"));
    }

    public List<Barber> getAll(Long adminId, Long barbershopId) {
        return barberRepository.findAll();

    }

    public Barber register(Long adminId, Long barbershopId, Barber barber) {
        BarberShop barberShop = barberShopService.findById(barbershopId);
        barber.setBarbershop(barberShop);

        return barberRepository.save(barber);
    }


    public Barber updateBarberProfile(Barber barber) {
        return barberRepository.save(barber);
    }

    public void changePassword(String mail, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

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

}
