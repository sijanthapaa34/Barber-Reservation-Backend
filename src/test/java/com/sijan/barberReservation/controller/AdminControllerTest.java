package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.AdminDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.AdminMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AdminMapper adminMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private BarberService barberService;

    @MockBean
    private BarberLeaveService barberLeaveService;

    @MockBean
    private PageMapper pageMapper;

    @MockBean
    private BarbershopMapper barbershopMapper;

    @Test
    @WithMockUser
    void getAllAppointment_Success() throws Exception {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);

        Page<Appointment> appointments = new PageImpl<>(List.of(new Appointment()));

        when(adminService.findById(anyLong())).thenReturn(admin);
        when(appointmentService.getAppointmentsForAdmin(any(), any())).thenReturn(appointments);
        when(pageMapper.toAppointmentPageResponse(any())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/admin/appointment")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(appointmentService, times(1)).getAppointmentsForAdmin(any(), any());
    }

    @Test
    @WithMockUser(roles = "MAIN_ADMIN")
    void getDashboardDetails_Success() throws Exception {
        // Arrange
        when(adminService.getDashboardData()).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/admin/main/dashboard"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).getDashboardData();
    }

    @Test
    @WithMockUser(roles = "SHOP_ADMIN")
    void getShopAdminDashboardDetails_Success() throws Exception {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);

        when(adminService.findById(1L)).thenReturn(admin);
        when(adminService.getShopAdminDashboardData(admin)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/admin/1/dashboard"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).getShopAdminDashboardData(admin);
    }

    @Test
    @WithMockUser(roles = "SHOP_ADMIN")
    void getShopByAdmin_Success() throws Exception {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setBarbershop(shop);

        when(adminService.findById(1L)).thenReturn(admin);
        when(barbershopMapper.toDTO(shop)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/admin/1/shop"))
                .andExpect(status().isOk());

        verify(barbershopMapper, times(1)).toDTO(shop);
    }

    @Test
    @WithMockUser(roles = "SHOP_ADMIN")
    void getShopByAdmin_NoShop_NotFound() throws Exception {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setBarbershop(null);

        when(adminService.findById(1L)).thenReturn(admin);

        // Act & Assert
        mockMvc.perform(get("/api/admin/1/shop"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateProfile_Success() throws Exception {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setPhone("1234567890");

        AdminDTO adminDTO = new AdminDTO();

        when(adminService.findById(1L)).thenReturn(admin);
        when(adminService.update(admin, "Updated Name", "1234567890")).thenReturn(admin);
        when(adminMapper.toDTO(admin)).thenReturn(adminDTO);

        // Act & Assert
        mockMvc.perform(put("/api/admin/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(adminService, times(1)).update(admin, "Updated Name", "1234567890");
    }

    @Test
    @WithMockUser
    void changePassword_Success() throws Exception {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(adminService.findById(1L)).thenReturn(admin);
        doNothing().when(adminService).changePassword(admin, "oldPassword", "newPassword");

        // Act & Assert
        mockMvc.perform(put("/api/admin/1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(adminService, times(1)).changePassword(admin, "oldPassword", "newPassword");
    }
}
