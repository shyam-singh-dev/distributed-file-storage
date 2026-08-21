package com.filestore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column (name = "content_type", nullable = false)
    private String contentType;

    @Column (name = "file_size",nullable = false)
    private Long fileSize;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "object_name", nullable = false, unique = true)
    private String objectName;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if(isDeleted == null) {
            isDeleted = false;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
