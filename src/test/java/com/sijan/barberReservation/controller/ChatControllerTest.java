package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.chat.*;
import com.sijan.barberReservation.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    private ConversationDTO conversationDTO;
    private MessageDTO messageDTO;

    @BeforeEach
    void setUp() {
        conversationDTO = new ConversationDTO();
        conversationDTO.setId(1L);
        conversationDTO.setCustomerId(1L);
        conversationDTO.setCustomerName("John Doe");
        conversationDTO.setShopAdminId(2L);
        conversationDTO.setShopAdminName("Admin");
        conversationDTO.setShopId(1L);
        conversationDTO.setShopName("Test Shop");
        conversationDTO.setLastMessage("Hello");
        conversationDTO.setLastMessageTime(LocalDateTime.now());
        conversationDTO.setUnreadCountCustomer(0);
        conversationDTO.setUnreadCountAdmin(0);
        conversationDTO.setCustomerOnline(false);
        conversationDTO.setAdminOnline(false);
        conversationDTO.setCustomerTyping(false);
        conversationDTO.setAdminTyping(false);

        messageDTO = new MessageDTO();
        messageDTO.setId(1L);
        messageDTO.setChatRoomId(1L);
        messageDTO.setSenderId(1L);
        messageDTO.setSenderName("John Doe");
        messageDTO.setSenderType("CUSTOMER");
        messageDTO.setMessageText("Hello");
        messageDTO.setStatus("SENT");
        messageDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void getOrCreateChatRoom_Success() throws Exception {
        // Arrange
        CreateChatRequest request = new CreateChatRequest();
        request.setCustomerId(1L);
        request.setCustomerName("John Doe");
        request.setShopAdminId(2L);
        request.setShopAdminName("Admin");
        request.setShopId(1L);
        request.setShopName("Test Shop");

        when(chatService.getOrCreateChatRoom(any(CreateChatRequest.class)))
                .thenReturn(conversationDTO);

        // Act & Assert
        mockMvc.perform(post("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.shopName").value("Test Shop"));
    }

    @Test
    @WithMockUser
    void getChatRooms_Success() throws Exception {
        // Arrange
        when(chatService.getChatRooms(1L, "CUSTOMER"))
                .thenReturn(List.of(conversationDTO));

        // Act & Assert
        mockMvc.perform(get("/api/chat/rooms")
                        .param("userId", "1")
                        .param("userType", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].shopName").value("Test Shop"));
    }

    @Test
    @WithMockUser
    void sendMessage_Success() throws Exception {
        // Arrange
        SendMessageRequest request = new SendMessageRequest();
        request.setChatRoomId(1L);
        request.setSenderId(1L);
        request.setSenderName("John Doe");
        request.setSenderType("CUSTOMER");
        request.setMessageText("Hello");

        when(chatService.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(messageDTO);

        // Act & Assert
        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.messageText").value("Hello"));
    }

    @Test
    @WithMockUser
    void getMessages_Success() throws Exception {
        // Arrange
        when(chatService.getMessages(1L))
                .thenReturn(List.of(messageDTO));

        // Act & Assert
        mockMvc.perform(get("/api/chat/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].messageText").value("Hello"));
    }

    @Test
    @WithMockUser
    void markMessagesAsRead_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/chat/messages/1/read")
                        .param("userType", "CUSTOMER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateTypingStatus_Success() throws Exception {
        // Arrange
        TypingStatusRequest request = new TypingStatusRequest();
        request.setChatRoomId(1L);
        request.setUserType("CUSTOMER");
        request.setIsTyping(true);

        // Act & Assert
        mockMvc.perform(post("/api/chat/typing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateOnlineStatus_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/chat/online")
                        .param("chatRoomId", "1")
                        .param("userType", "CUSTOMER")
                        .param("isOnline", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUnreadCount_Success() throws Exception {
        // Arrange
        when(chatService.getUnreadCount(1L, "CUSTOMER"))
                .thenReturn(5);

        // Act & Assert
        mockMvc.perform(get("/api/chat/unread-count")
                        .param("userId", "1")
                        .param("userType", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5));
    }
}
