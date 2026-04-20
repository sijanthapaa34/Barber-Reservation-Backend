package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.customer.CustomerNotFoundException;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.CustomerStatus;
import com.sijan.barberReservation.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPhone("1234567890");
        testCustomer.setPassword("encodedPassword");
        testCustomer.setPoints(50);
        testCustomer.setTotalBookings(5);
        testCustomer.setStatus(CustomerStatus.ACTIVE);
    }

    @Test
    void findById_ExistingCustomer_ReturnsCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        Customer result = customerService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Customer", result.getName());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingCustomer_ThrowsException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.findById(999L);
        });

        verify(customerRepository, times(1)).findById(999L);
    }

    @Test
    void update_ValidData_UpdatesCustomer() {
        String newName = "Updated Name";
        String newPhone = "9876543210";

        Customer result = customerService.update(testCustomer, newName, newPhone);

        assertEquals(newName, result.getName());
        assertEquals(newPhone, result.getPhone());
    }

    @Test
    void changePassword_CorrectCurrentPassword_ChangesPassword() {
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";

        when(passwordEncoder.matches(currentPassword, testCustomer.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        customerService.changePassword(testCustomer, currentPassword, newPassword);

        assertEquals(encodedNewPassword, testCustomer.getPassword());
        verify(passwordEncoder, times(1)).matches(currentPassword, "encodedPassword");
        verify(passwordEncoder, times(1)).encode(newPassword);
    }

    @Test
    void changePassword_IncorrectCurrentPassword_ThrowsException() {
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(passwordEncoder.matches(currentPassword, testCustomer.getPassword())).thenReturn(false);

        assertThrows(Exception.class, () -> {
            customerService.changePassword(testCustomer, currentPassword, newPassword);
        });

        verify(passwordEncoder, times(1)).matches(currentPassword, testCustomer.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void findAll_ReturnsPageOfCustomers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> customerPage = new PageImpl<>(Arrays.asList(testCustomer));

        when(customerRepository.findAll(pageable)).thenReturn(customerPage);

        Page<Customer> result = customerService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCustomer.getId(), result.getContent().get(0).getId());
        verify(customerRepository, times(1)).findAll(pageable);
    }

    @Test
    void findRegularCustomers_ReturnsCustomersWithMinBookings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> customerPage = new PageImpl<>(Arrays.asList(testCustomer));

        when(customerRepository.findByTotalBookingsGreaterThanEqual(3, pageable)).thenReturn(customerPage);

        Page<Customer> result = customerService.findRegularCustomers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(customerRepository, times(1)).findByTotalBookingsGreaterThanEqual(3, pageable);
    }

    @Test
    void findByShop_ReturnsShopCustomers() {
        Long shopId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> customerPage = new PageImpl<>(Arrays.asList(testCustomer));

        when(customerRepository.findCustomersByShopId(shopId, pageable)).thenReturn(customerPage);

        Page<Customer> result = customerService.findByShop(shopId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(customerRepository, times(1)).findCustomersByShopId(shopId, pageable);
    }

    @Test
    void findRegularCustomersByShop_ReturnsRegularShopCustomers() {
        Long shopId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> customerPage = new PageImpl<>(Arrays.asList(testCustomer));

        when(customerRepository.findRegularCustomersByShopId(shopId, 3, pageable)).thenReturn(customerPage);

        Page<Customer> result = customerService.findRegularCustomersByShop(shopId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(customerRepository, times(1)).findRegularCustomersByShopId(shopId, 3, pageable);
    }
}
