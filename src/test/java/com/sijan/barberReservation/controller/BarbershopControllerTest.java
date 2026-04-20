package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.UpdateBarbershopRequest;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.mapper.user.UpdateBarbershopRequestMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.AdminService;
import com.sijan.barberReservation.service.BarbershopService;
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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BarbershopController.class)
@AutoConfigureMockMvc(addFilters = false)
class BarbershopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BarbershopService barbershopService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private PageMapper pageMapper;

    @MockBean
    private BarbershopMapper barbershopMapper;

    @MockBean
    private UpdateBarbershopRequestMapper updateBarbershopRequestMapper;

    private Barbershop testBarbershop;
    private BarbershopDTO testBarbershopDTO;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Barbershop");
        testBarbershop.setAddress("123 Test St");
        testBarbershop.setPhone("1234567890");
        testBarbershop.setLatitude(new BigDecimal("27.7172"));
        testBarbershop.setLongitude(new BigDecimal("85.3240"));
        testBarbershop.setOperatingHours("9:00 AM - 6:00 PM");
        testBarbershop.setRating(4.5);
        testBarbershop.setActive(true);

        testBarbershopDTO = new BarbershopDTO();
        testBarbershopDTO.setId(1L);
        testBarbershopDTO.setName("Test Barbershop");
        testBarbershopDTO.setAddress("123 Test St");
        testBarbershopDTO.setPhone("1234567890");
        testBarbershopDTO.setRating(4.5);

        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setEmail("admin@test.com");
        testAdmin.setRole(Roles.SHOP_ADMIN);
        testAdmin.setBarbershop(testBarbershop);
    }

    @Test
    @WithMockUser
    void findById_Success() throws Exception {
        when(barbershopService.findById(1L)).thenReturn(testBarbershop);
        when(barbershopMapper.toDTO(testBarbershop)).thenReturn(testBarbershopDTO);

        mockMvc.perform(get("/api/barbershop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Barbershop"))
                .andExpect(jsonPath("$.address").value("123 Test St"));

        verify(barbershopService).findById(1L);
        verify(barbershopMapper).toDTO(testBarbershop);
    }

    @Test
    @WithMockUser
    void findById_NotFound() throws Exception {
        when(barbershopService.findById(999L))
                .thenThrow(new RuntimeException("Barbershop not found"));

        mockMvc.perform(get("/api/barbershop/999"))
                .andExpect(status().is5xxServerError());

        verify(barbershopService).findById(999L);
    }

    @Test
    @WithMockUser(roles = "SHOP_ADMIN")
    void update_Success() throws Exception {
        UpdateBarbershopRequest request = new UpdateBarbershopRequest();
        request.setName("Updated Barbershop");
        request.setAddress("456 New St");
        request.setPhone("9876543210");

        when(barbershopService.findById(1L)).thenReturn(testBarbershop);
        when(adminService.findById(anyLong())).thenReturn(testAdmin);
        when(updateBarbershopRequestMapper.toEntity(any(Barbershop.class), any(UpdateBarbershopRequest.class)))
                .thenReturn(testBarbershop);
        when(barbershopService.update(any(Barbershop.class), any(Admin.class))).thenReturn(testBarbershop);
        when(barbershopMapper.toDTO(testBarbershop)).thenReturn(testBarbershopDTO);

        mockMvc.perform(put("/api/barbershop/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(barbershopService).findById(1L);
        verify(barbershopService).update(any(Barbershop.class), any(Admin.class));
    }

    @Test
    @WithMockUser
    void findNearby_Success() throws Exception {
        List<Barbershop> shops = Arrays.asList(testBarbershop);
        Page<Barbershop> page = new PageImpl<>(shops, PageRequest.of(0, 10), 1);
        PageResponse<BarbershopDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testBarbershopDTO));
        pageResponse.setTotalElements(1L);

        when(barbershopService.findNearby(anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(page);
        when(pageMapper.toBarbershopPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/barbershop/nearby")
                        .param("latitude", "27.7172")
                        .param("longitude", "85.3240")
                        .param("radiusKm", "10.0")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(barbershopService).findNearby(eq(27.7172), eq(85.3240), eq(10.0), any());
    }

    @Test
    @WithMockUser
    void findNearby_MissingRequiredParams() throws Exception {
        mockMvc.perform(get("/api/barbershop/nearby")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getAll_Success() throws Exception {
        List<Barbershop> shops = Arrays.asList(testBarbershop);
        Page<Barbershop> page = new PageImpl<>(shops, PageRequest.of(0, 10), 1);
        PageResponse<BarbershopDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testBarbershopDTO));

        when(barbershopService.getAll(any())).thenReturn(page);
        when(pageMapper.toBarbershopPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/barbershop/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barbershopService).getAll(any());
    }

    @Test
    @WithMockUser
    void searchByWord_Success() throws Exception {
        List<Barbershop> shops = Arrays.asList(testBarbershop);
        Page<Barbershop> page = new PageImpl<>(shops, PageRequest.of(0, 10), 1);
        PageResponse<BarbershopDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testBarbershopDTO));

        when(barbershopService.searchByWord(eq("Test"), any())).thenReturn(page);
        when(pageMapper.toBarbershopPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/barbershop/search/Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barbershopService).searchByWord(eq("Test"), any());
    }

    @Test
    @WithMockUser
    void searchByWord_EmptyResults() throws Exception {
        Page<Barbershop> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        PageResponse<BarbershopDTO> emptyResponse = new PageResponse<>();
        emptyResponse.setContent(List.of());

        when(barbershopService.searchByWord(eq("NonExistent"), any())).thenReturn(emptyPage);
        when(pageMapper.toBarbershopPageResponse(emptyPage)).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/barbershop/search/NonExistent")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(barbershopService).searchByWord(eq("NonExistent"), any());
    }

    @Test
    @WithMockUser
    void getTopRated_Success() throws Exception {
        List<Barbershop> shops = Arrays.asList(testBarbershop);
        Page<Barbershop> page = new PageImpl<>(shops, PageRequest.of(0, 10), 1);
        PageResponse<BarbershopDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testBarbershopDTO));

        when(barbershopService.findTopRated(any())).thenReturn(page);
        when(pageMapper.toBarbershopPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/barbershop/top-rated")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(barbershopService).findTopRated(any());
    }

    @Test
    @WithMockUser
    void getAll_WithPagination() throws Exception {
        List<Barbershop> shops = Arrays.asList(testBarbershop);
        Page<Barbershop> page = new PageImpl<>(shops, PageRequest.of(2, 5), 15);
        PageResponse<BarbershopDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testBarbershopDTO));
        pageResponse.setTotalElements(15L);
        pageResponse.setPage(2);
        pageResponse.setSize(5);

        when(barbershopService.getAll(any())).thenReturn(page);
        when(pageMapper.toBarbershopPageResponse(page)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/barbershop/all")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(15));

        verify(barbershopService).getAll(any());
    }
}
