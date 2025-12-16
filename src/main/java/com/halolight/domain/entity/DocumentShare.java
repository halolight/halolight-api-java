package com.halolight.domain.entity;

import com.halolight.domain.entity.enums.SharePermission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "document_shares", indexes = {
        @Index(name = "idx_document_shares_document", columnList = "document_id"),
        @Index(name = "idx_document_shares_user", columnList = "shared_with_id")
})
public class DocumentShare {

    @Id
    @Column(nullable = false, updatable = false, length = 40)
    private String id;

    @Column(name = "document_id", nullable = false, length = 40)
    private String documentId;

    @Column(name = "shared_with_id", length = 40)
    private String sharedWithId;

    @Column(name = "team_id", length = 40)
    private String teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SharePermission permission = SharePermission.READ;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_id", insertable = false, updatable = false)
    private User sharedWith;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 25);
        }
    }
}
