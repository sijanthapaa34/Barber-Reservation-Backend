package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.CreateAppointmentMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private AppointmentDetailsMapper appointmentDetailsMapper;

    @MockBean
    private BarberService barberService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private ServiceOfferingService serviceOfferingService;

    @MockBean
    private CreateAppointmentMapper createAppointmentMapper;

    @MockBean
    private PageMapper pageMapper;

    @MockBean
    private UserService userService;

    private Appointment testAppointment;
    private AppointmentDetailsResponse testAppointmentResponse;
    private Customer testCustomer;
    private Barber testBarber;
    private ServiceOffering testService;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setEmail("customer@test.com");
        testCustomer.setName("Test Customer");
        testCustomer.setRole(Roles.CUSTOMER);

        testBarber = new Barber();
        testBarber.setId(1L);
        testBarber.setEmail("barber@test.com");
        testBarber.setName("Test Barber");
        testBarber.setRole(Roles.BARBER);

        testService = new ServiceOffering();
        testService.setId(1L);
        testService.setName("Haircut");
        testService.setDurationMinutes(30);

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setCustomer(testCustomer);
        testAppointment.setBarber(testBarber);
        testAppointment.setScheduledTime(LocalDateTime.now().plusDays(1));
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);

        testAppointmentResponse = new AppointmentDetailsResponse();
        testAppointmentResponse.setAppointmentId(1L);
        testAppointmentResponse.setCustomerName("Test Customer");
        testAppointmentResponse.setBarberName("Test Barber");
        testAppointmentResponse.setStatus(AppointmentStatus.CONFIRMED);
    }

    @Test
    @WithMockUser
    void findById_Success() throws Exception {
        when(appointmentService.findById(1L)).thenReturn(testAppointment);
        when(appointmentDetailsMapper.toDTO(testAppointment)).thenReturn(testAppointmentResponse);

        mockMvc.perform(get("/api/appointment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(1))
                .andExpect(jsonPath("$.customerName").value("Test Customer"))
                .andExpect(jsonPath("$.barberName").value("Test Barber"));

        verify(appointmentService).findById(1L);
        verify(appointmentDetailsMapper).toDTO(testAppointment);
    }

    @Test
    @WithMockUser
    void findById_NotFound() throws Exception {
        when(appointmentService.findById(999L))
                .thenThrow(new RuntimeException("Appointment not found"));

        mockMvc.perform(get("/api/appointment/999"))
                .andExpect(status().is5xxServerError());

        verify(appointmentService).findById(999L);
    }

    @Test
    @WithMockUser
    void reschedule_Success() throws Exception {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest();
        request.setNewDateTime(LocalDateTime.now().plusDays(2));

        when(appointmentService.findById(1L)).thenReturn(testAppointment);
        when(appointmentService.reschedule(any(Appointment.class), any(LocalDateTime.class)))
                .thenReturn(testAppointment);
        when(appointmentDetailsMapper.toDTO(testAppointment)).thenReturn(testAppointmentResponse);

        mockMvc.perform(put("/api/appointment/1/reschedule")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(1));

        verify(appointmentService).findById(1L);
        verify(appointmentService).reschedule(any(Appointment.class), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void upcomingByCustomer_Success() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        Page<Appointment> page = new PageImpl<>(appointments, PageRequest.of(0, 10), 1);
        PageResponse<AppointmentDetailsResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testAppointmentResponse));

        when(customerService.findById(anyLong())).thenReturn(testCustomer);
        when(appointmentService.getUpcomingByCustomer(any(Customer.class), anyInt(), anyInt()))
                .thenReturn(page);
        when(pageMapper.toAppointmentPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/appointment/upcoming")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].appointmentId").value(1));

        verify(customerService).findById(anyLong());
        verify(appointmentService).getUpcomingByCustomer(any(Customer.class), eq(0), eq(10));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void pastByCustomer_Success() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        Page<Appointment> page = new PageImpl<>(appointments, PageRequest.of(0, 10), 1);
        PageResponse<AppointmentDetailsResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testAppointmentResponse));

        when(customerService.findById(anyLong())).thenReturn(testCustomer);
        when(appointmentService.getPastByCustomer(any(Customer.class), anyInt(), anyInt()))
                .thenReturn(page);
        when(pageMapper.toAppointmentPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/appointment/past")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(customerService).findById(anyLong());
        verify(appointmentService).getPastByCustomer(any(Customer.class), eq(0), eq(10));
    }

    @Test
    @WithMockUser
    void upcomingByBarber_Success() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        Page<Appointment> page = new PageImpl<>(appointments, PageRequest.of(0, 10), 1);
        PageResponse<AppointmentDetailsResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testAppointmentResponse));

        when(barberService.findById(1L)).thenReturn(testBarber);
        when(appointmentService.getUpcomingByBarber(any(Barber.class), anyInt(), anyInt()))
                .thenReturn(page);
        when(pageMapper.toAppointmentPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/appointment/barber/1/upcoming")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barberService).findById(1L);
        verify(appointmentService).getUpcomingByBarber(any(Barber.class), eq(0), eq(10));
    }

    @Test
    @WithMockUser
    void pastByBarber_Success() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        Page<Appointment> page = new PageImpl<>(appointments, PageRequest.of(0, 10), 1);
        PageResponse<AppointmentDetailsResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testAppointmentResponse));

        when(barberService.findById(1L)).thenReturn(testBarber);
        when(appointmentService.getPastByBarber(any(Barber.class), anyInt(), anyInt()))
                .thenReturn(page);
        when(pageMapper.toAppointmentPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/appointment/barber/1/past")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barberService).findById(1L);
        verify(appointmentService).getPastByBarber(any(Barber.class), eq(0), eq(10));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void cancel_Success() throws Exception {
        when(userService.findById(anyLong())).thenReturn(testCustomer);
        doNothing().when(appointmentService).cancel(anyLong(), any(User.class));

        mockMvc.perform(put("/api/appointment/1/cancel")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).findById(anyLong());
        verify(appointmentService).cancel(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void getRefundPreview_Success() throws Exception {
        Map<String, Object> refundPreview = new HashMap<>();
        refundPreview.put("refundAmount", 80.0);
        refundPreview.put("refundPercentage", 80.0);
        refundPreview.put("cancellationFee", 20.0);

        when(appointmentService.getRefundPreview(anyLong(), anyLong())).thenReturn(refundPreview);

        mockMvc.perform(get("/api/appointment/1/refund-preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundAmount").value(80.0))
                .andExpect(jsonPath("$.refundPercentage").value(80.0))
                .andExpect(jsonPath("$.cancellationFee").value(20.0));

        verify(appointmentService).getRefundPreview(eq(1L), anyLong());
    }

    @Test
    @WithMockUser
    void getAvailableSlots_Success() throws Exception {
        AvailableSlotsResponseDTO response = AvailableSlotsResponseDTO.builder()
                .date(LocalDate.now().plusDays(1))
                .barberName("Test Barber")
                .availableSlots(Arrays.asList(
                        TimeSlotDTO.builder()
                                .date(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(10, 30))
                                .available(true)
                                .build()
                ))
                .build();

        when(barberService.findById(1L)).thenReturn(testBarber);
        when(serviceOfferingService.findByIds(anyList())).thenReturn(Arrays.asList(testService));
        when(appointmentService.getAvailability(any(Barber.class), anyList(), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/appointment/1/availability")
                        .param("serviceIds", "1")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barberName").value("Test Barber"))
                .andExpect(jsonPath("$.availableSlots").isArray());

        verify(barberService).findById(1L);
        verify(serviceOfferingService).findByIds(anyList());
        verify(appointmentService).getAvailability(any(Barber.class), anyList(), any(LocalDate.class));
    }

    @Test
    @WithMockUser
    void getAvailableSlots_MissingRequiredParams() throws Exception {
        mockMvc.perform(get("/api/appointment/1/availability")
                        .param("serviceIds", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getByBarber_Success() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        Page<Appointment> page = new PageImpl<>(appointments, PageRequest.of(0, 10), 1);
        PageResponse<AppointmentDetailsResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testAppointmentResponse));

        when(barberService.findById(1L)).thenReturn(testBarber);
        when(appointmentService.getBarberAppointments(any(Barber.class), any(), any(), any()))
                .thenReturn(page);
        when(pageMapper.toAppointmentPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/appointment/barber/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barberService).findById(1L);
        verify(appointmentService).getBarberAppointments(any(Barber.class), isNull(), isNull(), any());
    }

    @Test
    @WithMockUser
    void getByBarber_WithDateRange() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        Page<Appointment> page = new PageImpl<>(appointments, PageRequest.of(0, 10), 1);
        PageResponse<AppointmentDetailsResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testAppointmentResponse));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        when(barberService.findById(1L)).thenReturn(testBarber);
        when(appointmentService.getBarberAppointments(any(Barber.class), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(page);
        when(pageMapper.toAppointmentPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/appointment/barber/1")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barberService).findById(1L);
        verify(appointmentService).getBarberAppointments(any(Barber.class), eq(startDate), eq(endDate), any());
    }

    @Test
    @WithMockUser
    void getEarnings_Success() throws Exception {
        when(barberService.findById(1L)).thenReturn(testBarber);
        when(appointmentService.getEarnings(any(Barber.class), any(), any())).thenReturn(1500.0);

        mockMvc.perform(get("/api/appointment/barber/1/earnings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1500.0));

        verify(barberService).findById(1L);
        verify(appointmentService).getEarnings(any(Barber.class), isNull(), isNull());
    }

    @Test
    @WithMockUser(roles = "SHOP_ADMIN")
    void getShopAppointments_Success() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        Page<Appointment> page = new PageImpl<>(appointments, PageRequest.of(0, 10), 1);
        PageResponse<AppointmentDetailsResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testAppointmentResponse));

        when(appointmentService.getShopAppointments(anyLong(), anyString(), any()))
                .thenReturn(page);
        when(pageMapper.toAppointmentPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/appointment/shop/1/all")
                        .param("filter", "today")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(appointmentService).getShopAppointments(eq(1L), eq("today"), any());
    }

    @Test
    @WithMockUser(roles = "BARBER")
    void notifyCustomer_Success() throws Exception {
        when(appointmentService.findById(1L)).thenReturn(testAppointment);
        doNothing().when(appointmentService).sendManualReminder(any(Appointment.class));

        mockMvc.perform(post("/api/appointment/1/notify")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification sent to customer successfully"))
                .andExpect(jsonPath("$.customerName").value("Test Customer"));

        verify(appointmentService).findById(1L);
        verify(appointmentService).sendManualReminder(testAppointment);
    }
}
