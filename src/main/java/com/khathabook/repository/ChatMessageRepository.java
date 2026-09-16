package com.khathabook.repository;

import com.khathabook.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByOrderIdOrderByTimestampAsc(Long orderId);
    
    // To count unread messages for a specific party
    Integer countByOrderIdAndSenderRoleNotAndIsReadFalse(Long orderId, String role);
    
    // to update read statuses
    List<ChatMessage> findByOrderIdAndSenderRoleNotAndIsReadFalse(Long orderId, String role);
}
