package com.halolight.domain.repository;

import com.halolight.domain.entity.ConversationParticipant;
import com.halolight.domain.entity.id.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {

    List<ConversationParticipant> findByIdConversationId(String conversationId);

    List<ConversationParticipant> findByIdUserId(String userId);

    @Query("SELECT cp FROM ConversationParticipant cp WHERE cp.id.conversationId = :conversationId AND cp.id.userId = :userId")
    ConversationParticipant findByConversationIdAndUserId(
            @Param("conversationId") String conversationId,
            @Param("userId") String userId
    );

    @Modifying
    @Query("UPDATE ConversationParticipant cp SET cp.lastReadAt = :readAt WHERE cp.id.conversationId = :conversationId AND cp.id.userId = :userId")
    void updateLastReadAt(
            @Param("conversationId") String conversationId,
            @Param("userId") String userId,
            @Param("readAt") Instant readAt
    );

    @Query("SELECT COUNT(cp) FROM ConversationParticipant cp WHERE cp.id.conversationId = :conversationId")
    long countByConversationId(@Param("conversationId") String conversationId);

    void deleteByIdConversationId(String conversationId);

    boolean existsByIdConversationIdAndIdUserId(String conversationId, String userId);
}
