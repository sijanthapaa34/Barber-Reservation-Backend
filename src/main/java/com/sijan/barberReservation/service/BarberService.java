package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberLeave;
import com.sijan.barberReservation.model.LeaveStatus;
import com.sijan.barberReservation.repository.BarberLeaveRepository;
import com.sijan.barberReservation.repository.BarberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BarberService {

    private final BarberRepository barberRepository;
    private final BarberLeaveRepository barberLeaveRepository;

    public BarberService(BarberRepository barberRepository, BarberLeaveRepository barberLeaveRepository) {
        this.barberRepository = barberRepository;
        this.barberLeaveRepository = barberLeaveRepository;
    }
    public BarberDTO getBarberProfile(String mail) {
        Barber barber = barberRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        BarberDTO barberDTO = new BarberDTO();
        barberDTO.setName(barber.getName());
        barberDTO.setActive(barber.getActive());
        barberDTO.setEmail(barber.getEmail());
        barberDTO.setPhone(barber.getPhone());
        barberDTO.setBio(barber.getBio());
        barberDTO.setProfilePictureUrl(barber.getProfilePicture());
        barberDTO.setRating(barber.getRating());
        barberDTO.setCreatedAt(barber.getCreatedAt());

        return barberDTO;
    }

    public BarberDTO updateBarberProfile(String mail, UpdateUserRequest request) {
        Barber barber = barberRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        barber.setName(request.getName());
        barber.setPhone(request.getPhone());
        barber.setEmail(request.getEmail());

        barberRepository.save(barber);

        BarberDTO barberDTO = new BarberDTO();
        barberDTO.setName(barber.getName());
        barberDTO.setEmail(barber.getEmail());
        barberDTO.setPhone(barber.getPhone());
        barberDTO.setActive(barber.getActive());
        barberDTO.setBio(barber.getBio());
        barberDTO.setProfilePictureUrl(barber.getProfilePicture());
        barberDTO.setRating(barber.getRating());
        barberDTO.setCreatedAt(barber.getCreatedAt());

        return barberDTO;
    }

    public void changePassword(String mail, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        Barber barber = barberRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // In real system, compare encoded password using encoder.matches()
        // For now, assuming raw password check (not secure — just example)
        if (!barber.getPassword().equals(request.getCurrentPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Encode and save new password
//        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        barber.setPassword(request.getConfirmPassword());
        barberRepository.save(barber);
    }

    public void applyForLeave(String mail, LocalDate startDate, LocalDate endDate, String reason) {
        Barber barber = barberRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        // Prevent overlapping leaves
        List<BarberLeave> existingLeaves = barberLeaveRepository.findByEmail(mail);
        for (BarberLeave leave : existingLeaves) {
            if (leave.getStartDate().isBefore(endDate.plusDays(1)) &&
                    leave.getEndDate().isAfter(startDate.minusDays(1))) {
                throw new RuntimeException("Leave overlaps with existing leave: " + leave.getStartDate() + " to " + leave.getEndDate());
            }
        }

        // Create new leave
        BarberLeave newLeave = new BarberLeave();
        newLeave.setBarber(barber);
        newLeave.setStartDate(startDate);
        newLeave.setEndDate(endDate);
        newLeave.setReason(reason);
        newLeave.setStatus(LeaveStatus.PENDING);
        newLeave.setRequestedAt(LocalDateTime.now());

        barberLeaveRepository.save(newLeave);
    }
}
