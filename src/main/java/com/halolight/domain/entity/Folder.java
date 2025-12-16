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
@Table(name = "folders", indexes = {
        @Index(name = "idx_folders_owner", columnList = "owner_id"),
        @Index(name = "idx_folders_parent", columnList = "parent_id"),
        @Index(name = "idx_folders_team", columnList = "team_id")
})
public class Folder {

    @Id
    @Column(nullable = false, updatable = false, length = 40)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_id", length = 40)
    private String parentId;

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
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Folder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Folder> children = new LinkedHashSet<>();

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<StorageFile> files = new LinkedHashSet<>();

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
        if (this.ownerId == null && this.owner != null) {
            this.ownerId = this.owner.getId();
        }
        if (this.parentId == null && this.parent != null) {
            this.parentId = this.parent.getId();
        }
        if (this.teamId == null && this.team != null) {
            this.teamId = this.team.getId();
        }
    }
}
