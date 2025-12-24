package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    
    /**
     * Find the most recent message from a specific sender in a session
     * Used for notification throttling (5-minute rule)
     */
    List<Message> findTop1BySenderIdAndSessionIdOrderByCreatedAtDesc(Long senderId, Long sessionId);
}