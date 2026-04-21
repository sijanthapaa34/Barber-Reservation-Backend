package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.application.ApplicationDetailResponse;
import com.sijan.barberReservation.DTO.application.ApplicationRequest;
import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.Application;
import com.sijan.barberReservation.model.ApplicationType;
import com.sijan.barberReservation.service.ApplicationService;
import com.sijan.barberReservation.service.OtpService;
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

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private ApplicationMapper applicationMapper;

    @MockBean
    private PageMapper pageMapper;

    @Test
    @WithMockUser
    void findById_Success() throws Exception {
        // Arrange
        Application application = new Application();
        application.setId(1L);

        ApplicationDetailResponse response = new ApplicationDetailResponse();

        when(applicationService.findById(1L)).thenReturn(application);
        when(applicationMapper.toDTO(application)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).findById(1L);
    }

    @Test
    @WithMockUser
    void getForMainAdmin_Success() throws Exception {
        // Arrange
        Page<Application> applications = new PageImpl<>(List.of(new Application()));

        when(applicationService.getRelevantForMainAdmin(any())).thenReturn(applications);
        when(pageMapper.toApplicationPageResponse(any())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/applications/main-admin")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getRelevantForMainAdmin(any());
    }

    @Test
    @WithMockUser
    void getAllByBarbershop_Success() throws Exception {
        // Arrange
        Page<Application> applications = new PageImpl<>(List.of(new Application()));

        when(applicationService.getAllForShopAdmin(anyLong(), any())).thenReturn(applications);
        when(pageMapper.toApplicationPageResponse(any())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/applications/shop/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getAllForShopAdmin(anyLong(), any());
    }

    @Test
    @WithMockUser
    void submitApplication_Success() throws Exception {
        // Arrange
        ApplicationRequest request = new ApplicationRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setType("BARBER_SHOP");
        request.setShopName("Test Shop");

        Application application = new Application();
        ApplicationDetailResponse response = new ApplicationDetailResponse();

        when(otpService.verifyOtp("test@example.com", "123456")).thenReturn(true);
        when(applicationMapper.toEntity(request)).thenReturn(application);
        when(applicationService.save(application)).thenReturn(application);
        when(applicationMapper.toDTO(application)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).save(application);
    }

    @Test
    @WithMockUser
    void submitApplication_InvalidOtp_BadRequest() throws Exception {
        // Arrange
        ApplicationRequest request = new ApplicationRequest();
        request.setEmail("test@example.com");
        request.setOtp("000000");

        when(otpService.verifyOtp("test@example.com", "000000")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).save(any());
    }

    @Test
    @WithMockUser
    void approveByShopAdmin_Success() throws Exception {
        // Arrange
        doNothing().when(applicationService).approveByShopAdmin(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/applications/1/shop-approve"))
                .andExpect(status().isNoContent());

        verify(applicationService, times(1)).approveByShopAdmin(1L);
    }

    @Test
    @WithMockUser
    void approveByMainAdmin_Success() throws Exception {
        // Arrange
        doNothing().when(applicationService).approveByMainAdmin(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/applications/1/approve"))
                .andExpect(status().isNoContent());

        verify(applicationService, times(1)).approveByMainAdmin(1L);
    }

    @Test
    @WithMockUser
    void reject_Success() throws Exception {
        // Arrange
        Application application = new Application();
        application.setId(1L);

        when(applicationService.findById(1L)).thenReturn(application);
        doNothing().when(applicationService).reject(application);

        // Act & Assert
        mockMvc.perform(patch("/api/applications/1/reject"))
                .andExpect(status().isNoContent());

        verify(applicationService, times(1)).reject(application);
    }
}
