package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySessionIdOrderByTimestampDesc(Long sessionId);

    Page<Message> findBySessionIdOrderByTimestampDesc(Long sessionId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.session.id = :sessionId AND m.sender.id != :userId AND m.isRead = false")
    List<Message> findUnreadMessagesBySessionAndUser(Long sessionId, Long userId);

    List<Message> findBySessionIdAndSenderIdOrderByTimestampDesc(Long sessionId, Long senderId);

    long countBySessionId(Long sessionId);
}
