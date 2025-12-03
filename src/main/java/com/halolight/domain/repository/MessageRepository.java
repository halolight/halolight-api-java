package com.halolight.domain.repository;

import com.halolight.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    List<Message> findBySenderId(String senderId);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.createdAt > :after ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdAndCreatedAtAfter(
            @Param("conversationId") String conversationId,
            @Param("after") Instant after
    );

    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(String conversationId);

    long countByConversationId(String conversationId);

    @Query("SELECT COUNT(m) FROM Message m JOIN ConversationParticipant cp ON m.conversationId = cp.id.conversationId " +
            "WHERE cp.id.userId = :userId AND m.createdAt > COALESCE(cp.lastReadAt, m.createdAt)")
    long countUnreadByUserId(@Param("userId") String userId);

    void deleteByConversationId(String conversationId);
}
