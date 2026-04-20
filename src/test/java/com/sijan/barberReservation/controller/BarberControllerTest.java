package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.UpdateBarberRequest;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BarberController.class)
@AutoConfigureMockMvc(addFilters = false)
class BarberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BarberService barberService;

    @MockBean
    private BarbershopService barbershopService;

    @MockBean
    private BarberMapper barberMapper;

    @MockBean
    private PageMapper pageMapper;

    private Barber testBarber;
    private BarberDTO testBarberDTO;
    private Barbershop testBarbershop;

    @BeforeEach
    void setUp() {
        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Shop");

        testBarber = new Barber();
        testBarber.setId(1L);
        testBarber.setEmail("barber@test.com");
        testBarber.setName("Test Barber");
        testBarber.setBarbershop(testBarbershop);
        testBarber.setActive(true);

        testBarberDTO = new BarberDTO();
        testBarberDTO.setId(1L);
        testBarberDTO.setEmail("barber@test.com");
        testBarberDTO.setName("Test Barber");
    }

    @Test
    @WithMockUser
    void findById_Success() throws Exception {
        when(barberService.findById(1L)).thenReturn(testBarber);
        when(barberMapper.toDTO(testBarber)).thenReturn(testBarberDTO);

        mockMvc.perform(get("/api/barbers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Barber"));

        verify(barberService).findById(1L);
    }

    @Test
    @WithMockUser
    void getByBarbershop_Success() throws Exception {
        List<Barber> barbers = Arrays.asList(testBarber);
        Page<Barber> page = new PageImpl<>(barbers);
        PageResponse<BarberDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testBarberDTO));

        when(barbershopService.findById(1L)).thenReturn(testBarbershop);
        when(barberService.findByBarberShop(any(), any())).thenReturn(page);
        when(pageMapper.toBarberPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/barbers/barbershop/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barbershopService).findById(1L);
        verify(barberService).findByBarberShop(any(), any());
    }

    @Test
    @WithMockUser
    void updateProfile_Success() throws Exception {
        UpdateBarberRequest request = new UpdateBarberRequest();
        request.setBio("Updated bio");
        request.setExperienceYears(5);

        when(barberService.findById(1L)).thenReturn(testBarber);
        when(barberService.update(any(), anyString(), any(), anyInt(), any(), anyDouble())).thenReturn(testBarber);
        when(barberMapper.toDTO(testBarber)).thenReturn(testBarberDTO);

        mockMvc.perform(patch("/api/barbers/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(barberService).findById(1L);
        verify(barberService).update(any(), anyString(), any(), anyInt(), any(), anyDouble());
    }

    @Test
    @WithMockUser
    void changePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");

        when(barberService.findById(1L)).thenReturn(testBarber);
        doNothing().when(barberService).changePassword(any(), anyString(), anyString());

        mockMvc.perform(patch("/api/barbers/1/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(barberService).findById(1L);
        verify(barberService).changePassword(any(), anyString(), anyString());
    }

    @Test
    @WithMockUser
    void activate_Success() throws Exception {
        when(barberService.findById(1L)).thenReturn(testBarber);
        doNothing().when(barberService).activateBarber(testBarber);

        mockMvc.perform(patch("/api/barbers/1/activate/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(barberService).findById(1L);
        verify(barberService).activateBarber(testBarber);
    }

    @Test
    @WithMockUser
    void activate_WrongShop_Forbidden() throws Exception {
        Barbershop differentShop = new Barbershop();
        differentShop.setId(999L);
        testBarber.setBarbershop(differentShop);

        when(barberService.findById(1L)).thenReturn(testBarber);

        mockMvc.perform(patch("/api/barbers/1/activate/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(barberService).findById(1L);
        verify(barberService, never()).activateBarber(any());
    }

    @Test
    @WithMockUser
    void deactivate_Success() throws Exception {
        when(barberService.findById(1L)).thenReturn(testBarber);
        doNothing().when(barberService).deactivateBarber(testBarber);

        mockMvc.perform(patch("/api/barbers/1/deactivate/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(barberService).findById(1L);
        verify(barberService).deactivateBarber(testBarber);
    }

    @Test
    @WithMockUser
    void deactivate_WrongShop_Forbidden() throws Exception {
        Barbershop differentShop = new Barbershop();
        differentShop.setId(999L);
        testBarber.setBarbershop(differentShop);

        when(barberService.findById(1L)).thenReturn(testBarber);

        mockMvc.perform(patch("/api/barbers/1/deactivate/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(barberService).findById(1L);
        verify(barberService, never()).deactivateBarber(any());
    }
}
