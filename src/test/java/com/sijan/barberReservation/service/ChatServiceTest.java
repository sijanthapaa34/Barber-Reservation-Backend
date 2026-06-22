package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.chat.*;
import com.sijan.barberReservation.model.ChatMessage;
import com.sijan.barberReservation.model.ChatRoom;
import com.sijan.barberReservation.repository.ChatMessageRepository;
import com.sijan.barberReservation.repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ChatService chatService;

    private ChatRoom chatRoom;
    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        chatRoom = new ChatRoom();
        chatRoom.setId(1L);
        chatRoom.setCustomerId(1L);
        chatRoom.setCustomerName("John Doe");
        chatRoom.setShopAdminId(2L);
        chatRoom.setShopAdminName("Admin");
        chatRoom.setShopId(1L);
        chatRoom.setShopName("Test Shop");
        chatRoom.setUnreadCountCustomer(0);
        chatRoom.setUnreadCountAdmin(0);
        chatRoom.setCustomerOnline(false);
        chatRoom.setAdminOnline(false);
        chatRoom.setCustomerTyping(false);
        chatRoom.setAdminTyping(false);
        chatRoom.setCreatedAt(LocalDateTime.now());
        chatRoom.setLastMessageTime(LocalDateTime.now());

        chatMessage = new ChatMessage();
        chatMessage.setId(1L);
        chatMessage.setChatRoomId(1L);
        chatMessage.setSenderId(1L);
        chatMessage.setSenderName("John Doe");
        chatMessage.setSenderType(ChatMessage.SenderType.CUSTOMER);
        chatMessage.setMessageText("Hello");
        chatMessage.setStatus(ChatMessage.MessageStatus.SENT);
        chatMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getOrCreateChatRoom_ExistingRoom_ReturnsRoom() {
        // Arrange
        CreateChatRequest request = new CreateChatRequest();
        request.setCustomerId(1L);
        request.setCustomerName("John Doe");
        request.setShopAdminId(2L);
        request.setShopAdminName("Admin");
        request.setShopId(1L);
        request.setShopName("Test Shop");

        when(chatRoomRepository.findByCustomerIdAndShopAdminId(1L, 2L))
                .thenReturn(Optional.of(chatRoom));

        // Act
        ConversationDTO result = chatService.getOrCreateChatRoom(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getCustomerName());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void getOrCreateChatRoom_NewRoom_CreatesRoom() {
        // Arrange
        CreateChatRequest request = new CreateChatRequest();
        request.setCustomerId(1L);
        request.setCustomerName("John Doe");
        request.setShopAdminId(2L);
        request.setShopAdminName("Admin");
        request.setShopId(1L);
        request.setShopName("Test Shop");

        when(chatRoomRepository.findByCustomerIdAndShopAdminId(1L, 2L))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // Act
        ConversationDTO result = chatService.getOrCreateChatRoom(request);

        // Assert
        assertNotNull(result);
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    void sendMessage_CustomerMessage_IncrementsAdminUnreadCount() {
        // Arrange
        SendMessageRequest request = new SendMessageRequest();
        request.setChatRoomId(1L);
        request.setSenderId(1L);
        request.setSenderName("John Doe");
        request.setSenderType("CUSTOMER");
        request.setMessageText("Hello");

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
        when(chatMessageRepository.countByChatRoomIdAndSenderType(1L, ChatMessage.SenderType.CUSTOMER))
                .thenReturn(1L);

        // Act
        MessageDTO result = chatService.sendMessage(request);

        // Assert
        assertNotNull(result);
        assertEquals("Hello", result.getMessageText());
        verify(chatRoomRepository).save(argThat(room -> room.getUnreadCountAdmin() == 1));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1"), any(MessageDTO.class));
    }

    @Test
    void sendMessage_AdminMessage_IncrementsCustomerUnreadCount() {
        // Arrange
        SendMessageRequest request = new SendMessageRequest();
        request.setChatRoomId(1L);
        request.setSenderId(2L);
        request.setSenderName("Admin");
        request.setSenderType("ADMIN");
        request.setMessageText("Hi there");

        chatMessage.setSenderType(ChatMessage.SenderType.ADMIN);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // Act
        MessageDTO result = chatService.sendMessage(request);

        // Assert
        assertNotNull(result);
        verify(chatRoomRepository).save(argThat(room -> room.getUnreadCountCustomer() == 1));
    }

    @Test
    void getMessages_ReturnsAllMessages() {
        // Arrange
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(chatMessage));

        // Act
        List<MessageDTO> result = chatService.getMessages(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getMessageText());
    }

    @Test
    void getChatRooms_Customer_ReturnsCustomerRooms() {
        // Arrange
        when(chatRoomRepository.findByCustomerIdOrderByLastMessageTimeDesc(1L))
                .thenReturn(List.of(chatRoom));

        // Act
        List<ConversationDTO> result = chatService.getChatRooms(1L, "CUSTOMER");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Shop", result.get(0).getShopName());
    }

    @Test
    void getChatRooms_Admin_ReturnsAdminRooms() {
        // Arrange
        when(chatRoomRepository.findByShopAdminIdOrderByLastMessageTimeDesc(2L))
                .thenReturn(List.of(chatRoom));

        // Act
        List<ConversationDTO> result = chatService.getChatRooms(2L, "ADMIN");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getCustomerName());
    }

    @Test
    void markMessagesAsRead_Customer_ResetsCustomerUnreadCount() {
        // Arrange
        chatRoom.setUnreadCountCustomer(5);
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // Act
        chatService.markMessagesAsRead(1L, "CUSTOMER");

        // Assert
        verify(chatRoomRepository).save(argThat(room -> room.getUnreadCountCustomer() == 0));
        verify(chatMessageRepository).updateMessageStatus(eq(1L), 
                eq(ChatMessage.MessageStatus.READ), 
                eq(ChatMessage.SenderType.CUSTOMER));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1/read"), eq("CUSTOMER"));
    }

    @Test
    void markMessagesAsRead_Admin_ResetsAdminUnreadCount() {
        // Arrange
        chatRoom.setUnreadCountAdmin(3);
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // Act
        chatService.markMessagesAsRead(1L, "ADMIN");

        // Assert
        verify(chatRoomRepository).save(argThat(room -> room.getUnreadCountAdmin() == 0));
        verify(chatMessageRepository).updateMessageStatus(eq(1L), 
                eq(ChatMessage.MessageStatus.READ), 
                eq(ChatMessage.SenderType.ADMIN));
    }

    @Test
    void updateTypingStatus_Customer_UpdatesCustomerTyping() {
        // Arrange
        TypingStatusRequest request = new TypingStatusRequest();
        request.setChatRoomId(1L);
        request.setUserType("CUSTOMER");
        request.setIsTyping(true);

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // Act
        chatService.updateTypingStatus(request);

        // Assert
        verify(chatRoomRepository).save(argThat(room -> room.getCustomerTyping()));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1/typing"), eq(request));
    }

    @Test
    void updateTypingStatus_Admin_UpdatesAdminTyping() {
        // Arrange
        TypingStatusRequest request = new TypingStatusRequest();
        request.setChatRoomId(1L);
        request.setUserType("ADMIN");
        request.setIsTyping(false);

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // Act
        chatService.updateTypingStatus(request);

        // Assert
        verify(chatRoomRepository).save(argThat(room -> !room.getAdminTyping()));
    }

    @Test
    void updateOnlineStatus_Customer_UpdatesCustomerOnline() {
        // Arrange
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // Act
        chatService.updateOnlineStatus(1L, "CUSTOMER", true);

        // Assert
        verify(chatRoomRepository).save(argThat(room -> room.getCustomerOnline()));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1/online"), any(ChatService.OnlineStatusUpdate.class));
    }

    @Test
    void updateOnlineStatus_Admin_UpdatesAdminOnline() {
        // Arrange
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // Act
        chatService.updateOnlineStatus(1L, "ADMIN", false);

        // Assert
        verify(chatRoomRepository).save(argThat(room -> !room.getAdminOnline()));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1/online"), any(ChatService.OnlineStatusUpdate.class));
    }

    @Test
    void getUnreadCount_Customer_ReturnsTotalUnreadCount() {
        // Arrange
        chatRoom.setUnreadCountCustomer(5);
        ChatRoom chatRoom2 = new ChatRoom();
        chatRoom2.setUnreadCountCustomer(3);

        when(chatRoomRepository.findByCustomerIdOrderByLastMessageTimeDesc(1L))
                .thenReturn(List.of(chatRoom, chatRoom2));

        // Act
        Integer result = chatService.getUnreadCount(1L, "CUSTOMER");

        // Assert
        assertEquals(8, result);
    }

    @Test
    void getUnreadCount_Admin_ReturnsTotalUnreadCount() {
        // Arrange
        chatRoom.setUnreadCountAdmin(2);
        ChatRoom chatRoom2 = new ChatRoom();
        chatRoom2.setUnreadCountAdmin(4);

        when(chatRoomRepository.findByShopAdminIdOrderByLastMessageTimeDesc(2L))
                .thenReturn(List.of(chatRoom, chatRoom2));

        // Act
        Integer result = chatService.getUnreadCount(2L, "ADMIN");

        // Assert
        assertEquals(6, result);
    }
}
