package com.sijan.barberReservation.mapper.Leave;

import com.sijan.barberReservation.DTO.user.BarberLeaveDTO;
import com.sijan.barberReservation.DTO.user.LeaveRequestDTO;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberLeave;
import com.sijan.barberReservation.model.LeaveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LeaveMapperTest {

    private LeaveMapper leaveMapper;

    @BeforeEach
    void setUp() {
        leaveMapper = new LeaveMapper();
    }

    @Test
    void toDTO_WithApprovedLeave_ShouldMapWithApprovedAt() {
        // Arrange
        Barber barber = new Barber();
        barber.setName("John Barber");

        BarberLeave leave = new BarberLeave();
        leave.setId(1L);
        leave.setBarber(barber);
        leave.setStartDate(LocalDate.of(2023, 6, 1));
        leave.setEndDate(LocalDate.of(2023, 6, 5));
        leave.setReason("Vacation");
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setRequestedAt(LocalDateTime.of(2023, 5, 1, 10, 0));
        leave.setProcessedAt(LocalDateTime.of(2023, 5, 2, 14, 30));

        // Act
        BarberLeaveDTO dto = leaveMapper.toDTO(leave);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Barber", dto.getBarberName());
        assertEquals(LocalDate.of(2023, 6, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2023, 6, 5), dto.getEndDate());
        assertEquals("Vacation", dto.getReason());
        assertEquals("APPROVED", dto.getStatus());
        assertEquals(LocalDateTime.of(2023, 5, 1, 10, 0), dto.getRequestedAt());
        assertEquals(LocalDateTime.of(2023, 5, 2, 14, 30), dto.getApprovedAt());
        assertNull(dto.getRejectedAt());
    }

    @Test
    void toDTO_WithRejectedLeave_ShouldMapWithRejectedAt() {
        // Arrange
        Barber barber = new Barber();
        barber.setName("Jane Barber");

        BarberLeave leave = new BarberLeave();
        leave.setId(2L);
        leave.setBarber(barber);
        leave.setStartDate(LocalDate.of(2023, 7, 1));
        leave.setEndDate(LocalDate.of(2023, 7, 3));
        leave.setReason("Personal");
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRequestedAt(LocalDateTime.of(2023, 6, 1, 9, 0));
        leave.setProcessedAt(LocalDateTime.of(2023, 6, 2, 11, 0));

        // Act
        BarberLeaveDTO dto = leaveMapper.toDTO(leave);

        // Assert
        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("Jane Barber", dto.getBarberName());
        assertEquals(LocalDate.of(2023, 7, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2023, 7, 3), dto.getEndDate());
        assertEquals("Personal", dto.getReason());
        assertEquals("REJECTED", dto.getStatus());
        assertEquals(LocalDateTime.of(2023, 6, 1, 9, 0), dto.getRequestedAt());
        assertNull(dto.getApprovedAt());
        assertEquals(LocalDateTime.of(2023, 6, 2, 11, 0), dto.getRejectedAt());
    }

    @Test
    void toDTO_WithPendingLeave_ShouldNotMapProcessedAt() {
        // Arrange
        Barber barber = new Barber();
        barber.setName("Bob Barber");

        BarberLeave leave = new BarberLeave();
        leave.setId(3L);
        leave.setBarber(barber);
        leave.setStartDate(LocalDate.of(2023, 8, 1));
        leave.setEndDate(LocalDate.of(2023, 8, 2));
        leave.setReason("Medical");
        leave.setStatus(LeaveStatus.PENDING);
        leave.setRequestedAt(LocalDateTime.of(2023, 7, 1, 8, 0));

        // Act
        BarberLeaveDTO dto = leaveMapper.toDTO(leave);

        // Assert
        assertNotNull(dto);
        assertEquals(3L, dto.getId());
        assertEquals("Bob Barber", dto.getBarberName());
        assertEquals("PENDING", dto.getStatus());
        assertNull(dto.getApprovedAt());
        assertNull(dto.getRejectedAt());
    }

    @Test
    void toDTO_WithNullEntity_ShouldReturnNull() {
        // Act
        BarberLeaveDTO dto = leaveMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void toEntity_WithCompleteRequest_ShouldMapAllFields() {
        // Arrange
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setStartDate(LocalDate.of(2023, 9, 1));
        request.setEndDate(LocalDate.of(2023, 9, 5));
        request.setReason("Family event");

        // Act
        BarberLeave leave = leaveMapper.toEntity(request);

        // Assert
        assertNotNull(leave);
        assertEquals(LocalDate.of(2023, 9, 1), leave.getStartDate());
        assertEquals(LocalDate.of(2023, 9, 5), leave.getEndDate());
        assertEquals("Family event", leave.getReason());
    }

    @Test
    void toEntity_WithNullRequest_ShouldReturnNull() {
        // Act
        BarberLeave leave = leaveMapper.toEntity(null);

        // Assert
        assertNull(leave);
    }

    @Test
    void toEntity_ShouldNotSetStatusOrBarber() {
        // Arrange
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setStartDate(LocalDate.of(2023, 10, 1));
        request.setEndDate(LocalDate.of(2023, 10, 2));
        request.setReason("Test");

        // Act
        BarberLeave leave = leaveMapper.toEntity(request);

        // Assert
        assertNotNull(leave);
        // Status is not set by mapper, but may have default value from entity
        assertNull(leave.getBarber());
        assertNull(leave.getRequestedAt());
        assertNull(leave.getProcessedAt());
    }
}
