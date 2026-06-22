package com.sijan.barberReservation.mapper;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
import com.sijan.barberReservation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageMapperTest {

    @Mock
    private AppointmentDetailsMapper appointmentDetailsMapper;

    @Mock
    private BarberMapper barberMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private BarbershopMapper barbershopMapper;

    @InjectMocks
    private PageMapper pageMapper;

    private Appointment appointment;
    private Barber barber;
    private Customer customer;
    private Barbershop barbershop;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
        appointment.setId(1L);

        barber = new Barber();
        barber.setId(1L);
        barber.setName("John Barber");

        customer = new Customer();
        customer.setId(1L);
        customer.setName("Jane Customer");

        barbershop = new Barbershop();
        barbershop.setId(1L);
        barbershop.setName("Test Shop");
    }

    @Test
    void toAppointmentPageResponse_Success() {
        // Arrange
        Page<Appointment> page = new PageImpl<>(List.of(appointment), PageRequest.of(0, 10), 1);
        AppointmentDetailsResponse dto = new AppointmentDetailsResponse();
        dto.setAppointmentId(1L);

        when(appointmentDetailsMapper.toDTO(any(Appointment.class))).thenReturn(dto);

        // Act
        PageResponse<AppointmentDetailsResponse> result = pageMapper.toAppointmentPageResponse(page);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLast());
    }

    @Test
    void toBarberPageResponse_Success() {
        // Arrange
        Page<Barber> page = new PageImpl<>(List.of(barber), PageRequest.of(0, 10), 1);
        BarberDTO dto = new BarberDTO();
        dto.setId(1L);
        dto.setName("John Barber");

        when(barberMapper.toDTO(any(Barber.class))).thenReturn(dto);

        // Act
        PageResponse<BarberDTO> result = pageMapper.toBarberPageResponse(page);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John Barber", result.getContent().get(0).getName());
    }

    @Test
    void toCustomerPageResponse_Success() {
        // Arrange
        Page<Customer> page = new PageImpl<>(List.of(customer), PageRequest.of(0, 10), 1);
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("Jane Customer");

        when(customerMapper.toDTO(any(Customer.class))).thenReturn(dto);

        // Act
        PageResponse<CustomerDTO> result = pageMapper.toCustomerPageResponse(page);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Jane Customer", result.getContent().get(0).getName());
    }

    @Test
    void toBarbershopPageResponse_Success() {
        // Arrange
        Page<Barbershop> page = new PageImpl<>(List.of(barbershop), PageRequest.of(1, 5), 10);
        BarbershopDTO dto = new BarbershopDTO();
        dto.setId(1L);
        dto.setName("Test Shop");

        when(barbershopMapper.toDTO(any(Barbershop.class))).thenReturn(dto);

        // Act
        PageResponse<BarbershopDTO> result = pageMapper.toBarbershopPageResponse(page);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Shop", result.getContent().get(0).getName());
        assertEquals(1, result.getPage());
        assertEquals(5, result.getSize());
        assertEquals(10, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isLast());
    }

    @Test
    void toPageResponse_EmptyPage_ReturnsEmptyContent() {
        // Arrange
        Page<Customer> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        // Act
        PageResponse<CustomerDTO> result = pageMapper.toCustomerPageResponse(page);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isLast());
    }

    @Test
    void toPageResponse_MultiplePages_CorrectPagination() {
        // Arrange
        Page<Barber> page = new PageImpl<>(List.of(barber), PageRequest.of(2, 10), 21);
        BarberDTO dto = new BarberDTO();
        dto.setId(1L);

        when(barberMapper.toDTO(any(Barber.class))).thenReturn(dto);

        // Act
        PageResponse<BarberDTO> result = pageMapper.toBarberPageResponse(page);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getPage());
        assertEquals(21, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertTrue(result.isLast());
    }
}
