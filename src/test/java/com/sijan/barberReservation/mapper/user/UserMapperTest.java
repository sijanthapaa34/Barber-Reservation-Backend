package com.sijan.barberReservation.mapper.user;

import com.sijan.barberReservation.DTO.user.UserDTO;
import com.sijan.barberReservation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toDTO_WithRegularUser_ShouldMapAllFields() {
        // Arrange
        Customer user = new Customer();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPhone("1234567890");
        user.setActive(true);
        user.setRole(Roles.CUSTOMER);
        user.setProfilePicture("http://example.com/pic.jpg");

        // Act
        UserDTO dto = userMapper.toDTO(user);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("1234567890", dto.getPhone());
        assertTrue(dto.getActive());
        assertEquals(Roles.CUSTOMER, dto.getRole());
        assertEquals("http://example.com/pic.jpg", dto.getProfilePicture());
        assertNull(dto.getShopId());
    }

    @Test
    void toDTO_WithAdminWithoutBarbershop_ShouldMapWithoutShopId() {
        // Arrange
        Admin admin = new Admin();
        admin.setId(2L);
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPhone("9876543210");
        admin.setActive(true);
        admin.setRole(Roles.MAIN_ADMIN);
        admin.setProfilePicture("http://example.com/admin.jpg");
        admin.setBarbershop(null);

        // Act
        UserDTO dto = userMapper.toDTO(admin);

        // Assert
        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("Admin User", dto.getName());
        assertEquals("admin@example.com", dto.getEmail());
        assertEquals("9876543210", dto.getPhone());
        assertTrue(dto.getActive());
        assertEquals(Roles.MAIN_ADMIN, dto.getRole());
        assertEquals("http://example.com/admin.jpg", dto.getProfilePicture());
        assertNull(dto.getShopId());
    }

    @Test
    void toDTO_WithAdminWithBarbershop_ShouldMapWithShopId() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(10L);
        shop.setName("Test Shop");

        Admin admin = new Admin();
        admin.setId(3L);
        admin.setName("Shop Admin");
        admin.setEmail("shopadmin@example.com");
        admin.setPhone("5555555555");
        admin.setActive(true);
        admin.setRole(Roles.SHOP_ADMIN);
        admin.setProfilePicture("http://example.com/shopadmin.jpg");
        admin.setBarbershop(shop);

        // Act
        UserDTO dto = userMapper.toDTO(admin);

        // Assert
        assertNotNull(dto);
        assertEquals(3L, dto.getId());
        assertEquals("Shop Admin", dto.getName());
        assertEquals("shopadmin@example.com", dto.getEmail());
        assertEquals("5555555555", dto.getPhone());
        assertTrue(dto.getActive());
        assertEquals(Roles.SHOP_ADMIN, dto.getRole());
        assertEquals("http://example.com/shopadmin.jpg", dto.getProfilePicture());
        assertEquals(10L, dto.getShopId());
    }

    @Test
    void toDTO_WithBarber_ShouldMapAllFields() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(5L);
        shop.setName("Barber Shop");

        Barber barber = new Barber();
        barber.setId(4L);
        barber.setName("Barber Joe");
        barber.setEmail("barber@example.com");
        barber.setPhone("1111111111");
        barber.setActive(true);
        barber.setRole(Roles.BARBER);
        barber.setProfilePicture("http://example.com/barber.jpg");
        barber.setBarbershop(shop);

        // Act
        UserDTO dto = userMapper.toDTO(barber);

        // Assert
        assertNotNull(dto);
        assertEquals(4L, dto.getId());
        assertEquals("Barber Joe", dto.getName());
        assertEquals("barber@example.com", dto.getEmail());
        assertEquals("1111111111", dto.getPhone());
        assertTrue(dto.getActive());
        assertEquals(Roles.BARBER, dto.getRole());
        assertEquals("http://example.com/barber.jpg", dto.getProfilePicture());
        assertNull(dto.getShopId()); // Barber is not Admin, so no shopId
    }

    @Test
    void toDTO_WithInactiveUser_ShouldMapInactiveStatus() {
        // Arrange
        Customer user = new Customer();
        user.setId(5L);
        user.setName("Inactive User");
        user.setEmail("inactive@example.com");
        user.setPhone("0000000000");
        user.setActive(false);
        user.setRole(Roles.CUSTOMER);

        // Act
        UserDTO dto = userMapper.toDTO(user);

        // Assert
        assertNotNull(dto);
        assertFalse(dto.getActive());
    }

    @Test
    void toDTO_WithNullProfilePicture_ShouldMapNull() {
        // Arrange
        Customer user = new Customer();
        user.setId(6L);
        user.setName("No Pic User");
        user.setEmail("nopic@example.com");
        user.setPhone("2222222222");
        user.setActive(true);
        user.setRole(Roles.CUSTOMER);
        user.setProfilePicture(null);

        // Act
        UserDTO dto = userMapper.toDTO(user);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getProfilePicture());
    }
}
