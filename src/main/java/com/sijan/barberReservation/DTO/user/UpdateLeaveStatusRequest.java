package com.sijan.barberReservation.DTO.user;

import com.sijan.barberReservation.model.LeaveStatus;
import lombok.Data;

@Data
public class UpdateLeaveStatusRequest {
    private LeaveStatus status;
}
