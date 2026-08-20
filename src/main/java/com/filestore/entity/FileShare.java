package com.filestore.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_share",uniqueConstraints = @UniqueConstraint(columnNames = {"file_id","shared_with_email"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class FileShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id",nullable = false)
    private FileMetadata file;

    @Column(name = "shared_by_email",nullable = false)
    private String sharedByEmail;

    @Column(name = "shared_with_email",nullable = false)
    private String sharedWithEmail;

    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreated() {
        createdAt = LocalDateTime.now();

    }

}
