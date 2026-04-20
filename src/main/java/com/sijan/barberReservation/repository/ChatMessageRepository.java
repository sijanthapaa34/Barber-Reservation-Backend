package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
    
    long countByChatRoomIdAndSenderType(Long chatRoomId, ChatMessage.SenderType senderType);
    
    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = :status WHERE m.chatRoomId = :chatRoomId AND m.senderType != :excludeSenderType AND m.status != 'READ'")
    void updateMessageStatus(@Param("chatRoomId") Long chatRoomId, 
                            @Param("status") ChatMessage.MessageStatus status,
                            @Param("excludeSenderType") ChatMessage.SenderType excludeSenderType);
}
