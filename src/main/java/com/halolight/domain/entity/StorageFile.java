package com.halolight.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_files_owner", columnList = "owner_id"),
        @Index(name = "idx_files_folder", columnList = "folder_id"),
        @Index(name = "idx_files_team", columnList = "team_id")
})
public class StorageFile {

    @Id
    @Column(nullable = false, updatable = false, length = 40)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    @Builder.Default
    private BigInteger size = BigInteger.ZERO;

    @Column(nullable = false)
    private String path;

    @Column(name = "folder_id", length = 40)
    private String folderId;

    @Column(name = "owner_id", nullable = false, length = 40)
    private String ownerId;

    @Column(name = "team_id", length = 40)
    private String teamId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", insertable = false, updatable = false)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

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
