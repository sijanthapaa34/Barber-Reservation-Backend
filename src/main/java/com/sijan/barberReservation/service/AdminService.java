package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    public Admin findById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(()-> new RuntimeException("Admin id not found"));
    }
    public void register(Admin admin) {
        adminRepository.save(admin);
    }

    public Admin findByEmail(String adminEmail) {
        return adminRepository.findByEmail(adminEmail);
    }
}
