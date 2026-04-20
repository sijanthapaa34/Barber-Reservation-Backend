package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.service.RegisterServiceRequest;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.DTO.service.ServiceUpdateRequest;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
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

@WebMvcTest(ServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServiceOfferingService serviceOfferingService;

    @MockBean
    private BarbershopService barbershopService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private ServiceMapper serviceMapper;

    @MockBean
    private PageMapper pageMapper;

    private ServiceOffering testService;
    private ServiceDTO testServiceDTO;
    private Barbershop testBarbershop;

    @BeforeEach
    void setUp() {
        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Shop");

        testService = new ServiceOffering();
        testService.setId(1L);
        testService.setName("Haircut");
        testService.setPrice(25.00);
        testService.setDurationMinutes(30);
        testService.setBarbershop(testBarbershop);
        testService.setAvailable(true);

        testServiceDTO = new ServiceDTO();
        testServiceDTO.setId(1L);
        testServiceDTO.setName("Haircut");
        testServiceDTO.setPrice(25.00);
        testServiceDTO.setDurationMinutes(30);
    }

    @Test
    @WithMockUser
    void findById_Success() throws Exception {
        when(serviceOfferingService.findById(1L)).thenReturn(testService);
        when(serviceMapper.toDTO(testService)).thenReturn(testServiceDTO);

        mockMvc.perform(get("/api/service/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Haircut"));

        verify(serviceOfferingService).findById(1L);
    }

    @Test
    @WithMockUser
    void getAll_Success() throws Exception {
        List<ServiceOffering> services = Arrays.asList(testService);
        Page<ServiceOffering> page = new PageImpl<>(services);
        PageResponse<ServiceDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testServiceDTO));

        when(serviceOfferingService.getAll(any())).thenReturn(page);
        when(pageMapper.toServicePageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/service/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(serviceOfferingService).getAll(any());
    }

    @Test
    @WithMockUser
    void getAllByBarbershop_Success() throws Exception {
        List<ServiceOffering> services = Arrays.asList(testService);
        Page<ServiceOffering> page = new PageImpl<>(services);
        PageResponse<ServiceDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testServiceDTO));

        when(barbershopService.findById(1L)).thenReturn(testBarbershop);
        when(serviceOfferingService.getAllByBarbershop(any(), any())).thenReturn(page);
        when(pageMapper.toServicePageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/service/barbershop/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barbershopService).findById(1L);
        verify(serviceOfferingService).getAllByBarbershop(any(), any());
    }

    @Test
    @WithMockUser
    void add_Success() throws Exception {
        RegisterServiceRequest request = new RegisterServiceRequest();
        request.setName("New Service");
        request.setPrice(30.00);
        request.setDurationMinutes(45);

        when(serviceMapper.toEntity(any())).thenReturn(testService);
        when(barbershopService.findById(1L)).thenReturn(testBarbershop);
        when(serviceOfferingService.add(any(), any())).thenReturn(testService);
        when(serviceMapper.toDTO(testService)).thenReturn(testServiceDTO);

        mockMvc.perform(post("/api/service/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(barbershopService).findById(1L);
        verify(serviceOfferingService).add(any(), any());
    }

    @Test
    @WithMockUser
    void update_Success() throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setDurationMinutes(60);
        request.setPrice(35.00);

        when(serviceOfferingService.findById(1L)).thenReturn(testService);
        when(serviceOfferingService.update(any(), anyInt(), any(), any())).thenReturn(testService);
        when(serviceMapper.toDTO(testService)).thenReturn(testServiceDTO);

        mockMvc.perform(patch("/api/service/1/update/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(serviceOfferingService).findById(1L);
        verify(serviceOfferingService).update(any(), anyInt(), any(), any());
    }

    @Test
    @WithMockUser
    void update_WrongShop_Forbidden() throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setDurationMinutes(60);

        Barbershop differentShop = new Barbershop();
        differentShop.setId(999L);
        testService.setBarbershop(differentShop);

        when(serviceOfferingService.findById(1L)).thenReturn(testService);

        mockMvc.perform(patch("/api/service/1/update/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(serviceOfferingService).findById(1L);
        verify(serviceOfferingService, never()).update(any(), anyInt(), any(), any());
    }

    @Test
    @WithMockUser
    void activateService_Success() throws Exception {
        when(serviceOfferingService.findById(1L)).thenReturn(testService);
        doNothing().when(serviceOfferingService).activateService(testService);

        mockMvc.perform(patch("/api/service/1/activate/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(serviceOfferingService).findById(1L);
        verify(serviceOfferingService).activateService(testService);
    }

    @Test
    @WithMockUser
    void activateService_WrongShop_Forbidden() throws Exception {
        Barbershop differentShop = new Barbershop();
        differentShop.setId(999L);
        testService.setBarbershop(differentShop);

        when(serviceOfferingService.findById(1L)).thenReturn(testService);

        mockMvc.perform(patch("/api/service/1/activate/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(serviceOfferingService).findById(1L);
        verify(serviceOfferingService, never()).activateService(any());
    }

    @Test
    @WithMockUser
    void deactivateService_Success() throws Exception {
        when(serviceOfferingService.findById(1L)).thenReturn(testService);
        doNothing().when(serviceOfferingService).deactivateService(testService);

        mockMvc.perform(patch("/api/service/1/deactivate/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(serviceOfferingService).findById(1L);
        verify(serviceOfferingService).deactivateService(testService);
    }

    @Test
    @WithMockUser
    void deactivateService_WrongShop_Forbidden() throws Exception {
        Barbershop differentShop = new Barbershop();
        differentShop.setId(999L);
        testService.setBarbershop(differentShop);

        when(serviceOfferingService.findById(1L)).thenReturn(testService);

        mockMvc.perform(patch("/api/service/1/deactivate/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(serviceOfferingService).findById(1L);
        verify(serviceOfferingService, never()).deactivateService(any());
    }
}
