package com.sijan.barberReservation.mapper.Leave;

import com.sijan.barberReservation.DTO.user.BarberLeaveDTO;
import com.sijan.barberReservation.DTO.user.LeaveRequestDTO;
import com.sijan.barberReservation.model.BarberLeave;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {
    public BarberLeaveDTO toDTO(BarberLeave entity) {
        if (entity == null) return null;

        BarberLeaveDTO dto = new BarberLeaveDTO();
        dto.setId(entity.getId());
        dto.setBarberName(entity.getBarber().getName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus().name());
        dto.setRequestedAt(entity.getRequestedAt());

        if (entity.getStatus() == com.sijan.barberReservation.model.LeaveStatus.APPROVED) {
            dto.setApprovedAt(entity.getProcessedAt());
        } else if (entity.getStatus() == com.sijan.barberReservation.model.LeaveStatus.REJECTED) {
            dto.setRejectedAt(entity.getProcessedAt());
        }

        return dto;
    }
    public BarberLeave toEntity(LeaveRequestDTO dto) {
        if (dto == null) return null;

        BarberLeave entity = new BarberLeave();
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setReason(dto.getReason());

        return entity;
    }
}
