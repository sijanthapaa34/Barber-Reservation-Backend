package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CustomerMapper customerMapper;

    @MockBean
    private PageMapper pageMapper;

    private Customer testCustomer;
    private CustomerDTO testCustomerDTO;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setPoints(150);
        testCustomer.setTotalBookings(10);

        testCustomerDTO = new CustomerDTO();
        testCustomerDTO.setId(1L);
        testCustomerDTO.setName("Test Customer");
        testCustomerDTO.setPoints(150);
    }

    @Test
    @WithMockUser
    void getCustomerById_Success() throws Exception {
        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(customerMapper.toDTO(any())).thenReturn(testCustomerDTO);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Customer"));

        verify(customerService).findById(1L);
    }

    @Test
    @WithMockUser
    void getCustomerLoyalty_Success() throws Exception {
        when(customerService.findById(1L)).thenReturn(testCustomer);

        mockMvc.perform(get("/api/customers/1/loyalty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(150))
                .andExpect(jsonPath("$.freeAppointmentsEarned").value(1))
                .andExpect(jsonPath("$.pointsToNextReward").value(50))
                .andExpect(jsonPath("$.progressPercent").value(50));

        verify(customerService).findById(1L);
    }

    @Test
    @WithMockUser
    void getAllCustomers_Success() throws Exception {
        Page<Customer> page = new PageImpl<>(Arrays.asList(testCustomer));
        PageResponse<CustomerDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testCustomerDTO));

        when(customerService.findAll(any())).thenReturn(page);
        when(pageMapper.toCustomerPageResponse(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/customers")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "totalBookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(customerService).findAll(any());
    }

    @Test
    @WithMockUser
    void getRegularCustomers_Success() throws Exception {
        Page<Customer> page = new PageImpl<>(Arrays.asList(testCustomer));
        PageResponse<CustomerDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testCustomerDTO));

        when(customerService.findRegularCustomers(any())).thenReturn(page);
        when(pageMapper.toCustomerPageResponse(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/customers/regular")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(customerService).findRegularCustomers(any());
    }

    @Test
    @WithMockUser
    void getShopCustomers_Success() throws Exception {
        Page<Customer> page = new PageImpl<>(Arrays.asList(testCustomer));
        PageResponse<CustomerDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testCustomerDTO));

        when(customerService.findByShop(anyLong(), any())).thenReturn(page);
        when(pageMapper.toCustomerPageResponse(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/customers/shop/1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "totalBookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(customerService).findByShop(anyLong(), any());
    }

    @Test
    @WithMockUser
    void getShopRegularCustomers_Success() throws Exception {
        Page<Customer> page = new PageImpl<>(Arrays.asList(testCustomer));
        PageResponse<CustomerDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testCustomerDTO));

        when(customerService.findRegularCustomersByShop(anyLong(), any())).thenReturn(page);
        when(pageMapper.toCustomerPageResponse(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/customers/shop/1/regular")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(customerService).findRegularCustomersByShop(anyLong(), any());
    }
}
