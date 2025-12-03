package com.halolight.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversations_team", columnList = "team_id"),
        @Index(name = "idx_conversations_updated", columnList = "updated_at")
})
public class Conversation {

    @Id
    @Column(nullable = false, updatable = false, length = 40)
    private String id;

    private String name;

    @Column(name = "is_group", nullable = false)
    @Builder.Default
    private Boolean isGroup = false;

    @Column(name = "team_id", length = 40)
    private String teamId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ConversationParticipant> participants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Message> messages = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 25);
        }
    }
}
