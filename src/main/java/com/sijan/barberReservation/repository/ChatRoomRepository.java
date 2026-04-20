package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    Optional<ChatRoom> findByCustomerIdAndShopAdminId(Long customerId, Long shopAdminId);
    
    List<ChatRoom> findByCustomerIdOrderByLastMessageTimeDesc(Long customerId);
    
    List<ChatRoom> findByShopAdminIdOrderByLastMessageTimeDesc(Long shopAdminId);
}
