package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.user.BarberLeaveDTO;
import com.sijan.barberReservation.model.BarberLeave;
import org.springframework.stereotype.Component;

@Component
public class BarberLeaveMapper {
    public BarberLeaveDTO toDTO(BarberLeave leave) {
        if (leave == null) return null;

        return new BarberLeaveDTO(
                leave.getId(),
                leave.getBarber() != null ? leave.getBarber().getName() : null,
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                leave.getRequestedAt(),
                leave.getApprovedAt(),
                leave.getRejectedAt()
        );
    }

}
