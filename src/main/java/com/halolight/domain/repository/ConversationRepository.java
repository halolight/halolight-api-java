package com.halolight.domain.repository;

import com.halolight.domain.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findByTeamId(String teamId);

    @Query("SELECT c FROM Conversation c JOIN c.participants cp WHERE cp.user.id = :userId ORDER BY c.updatedAt DESC")
    Page<Conversation> findByParticipantUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT c FROM Conversation c JOIN c.participants cp WHERE " +
            "c.isGroup = false AND cp.user.id IN (:userId1, :userId2) " +
            "GROUP BY c HAVING COUNT(DISTINCT cp.user.id) = 2")
    Optional<Conversation> findDirectConversation(@Param("userId1") String userId1, @Param("userId2") String userId2);

    @Query("SELECT c FROM Conversation c JOIN c.participants cp WHERE " +
            "cp.user.id = :userId AND c.isGroup = :isGroup")
    List<Conversation> findByParticipantUserIdAndIsGroup(
            @Param("userId") String userId,
            @Param("isGroup") Boolean isGroup
    );
}
