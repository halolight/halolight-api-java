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
@Table(name = "teams")
public class Team {

    @Id
    @Column(nullable = false, updatable = false, length = 40)
    private String id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String avatar;

    @Column(name = "owner_id", nullable = false, length = 40)
    private String ownerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TeamMember> members = new LinkedHashSet<>();

    @OneToMany(mappedBy = "team")
    @Builder.Default
    private Set<Document> documents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "team")
    @Builder.Default
    private Set<StorageFile> files = new LinkedHashSet<>();

    @OneToMany(mappedBy = "team")
    @Builder.Default
    private Set<Folder> folders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "team")
    @Builder.Default
    private Set<DocumentShare> shares = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 25);
        }
    }
}
