package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.NotificationRepository;
import com.sijan.barberReservation.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationRepository notificationRepository;

    private Notification testNotification;
    private User testUser;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        testUser = new Customer();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setRole(Roles.CUSTOMER);
        testUser.setActive(true);

        testUserPrincipal = new UserPrincipal(testUser);

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("Test message");
        testNotification.setRead(false);
        testNotification.setTimestamp(LocalDateTime.now());
        testNotification.setUser(testUser);
    }

    @Test
    void saveToken_Success() throws Exception {
        doNothing().when(notificationService).saveToken(anyLong(), anyString(), anyString());

        mockMvc.perform(post("/api/notifications/token")
                        .with(csrf())
                        .with(user(testUserPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"test-token\""))
                .andExpect(status().isOk());

        verify(notificationService).saveToken(anyLong(), anyString(), anyString());
    }

    @Test
    void getNotifications_Success() throws Exception {
        when(notificationRepository.findByUserIdOrderByTimestampDesc(anyLong()))
                .thenReturn(Arrays.asList(testNotification));

        mockMvc.perform(get("/api/notifications")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Notification"));

        verify(notificationRepository).findByUserIdOrderByTimestampDesc(anyLong());
    }

    @Test
    void getUnreadCount_Success() throws Exception {
        when(notificationRepository.countByUserIdAndIsReadFalse(anyLong())).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(notificationRepository).countByUserIdAndIsReadFalse(anyLong());
    }

    @Test
    void markAsRead_Success() throws Exception {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any())).thenReturn(testNotification);

        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf())
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk());

        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any());
    }

    @Test
    void markAllAsRead_Success() throws Exception {
        when(notificationRepository.findUnreadByUserId(anyLong()))
                .thenReturn(Arrays.asList(testNotification));
        when(notificationRepository.saveAll(any())).thenReturn(Arrays.asList(testNotification));

        mockMvc.perform(put("/api/notifications/read-all")
                        .with(csrf())
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk());

        verify(notificationRepository).findUnreadByUserId(anyLong());
        verify(notificationRepository).saveAll(any());
    }

    @Test
    @WithMockUser
    void sendChatNotification_Success() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("recipientId", "1");
        payload.put("recipientType", "CUSTOMER");
        payload.put("senderName", "Test Sender");
        payload.put("messagePreview", "Hello!");
        payload.put("chatId", "123");

        doNothing().when(notificationService).sendPushNotification(anyLong(), anyString(), anyString(), anyString(), any());

        mockMvc.perform(post("/api/notifications/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(notificationService).sendPushNotification(anyLong(), anyString(), anyString(), anyString(), any());
    }
}
