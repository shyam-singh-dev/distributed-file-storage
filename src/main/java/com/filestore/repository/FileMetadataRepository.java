package com.filestore.repository;


import com.filestore.entity.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata,Long> {


    // Paginated version
    Page<FileMetadata> findByUploadedByAndIsDeletedFalse(
            String uploadedBy, Pageable pageable);

    // find all files by uploader email (not deleted)
    List<FileMetadata> findByUploadedByAndIsDeletedFalse(String uploadedBy);

    // find specific file by id and uploader
    Optional<FileMetadata> findByIdAndUploadedByAndIsDeletedFalse (Long id, String uploadedBy);

    // check if file exists with this object name
    boolean existsByObjectName(String objectName);

    // find all non-deleted files (admin use)
    List<FileMetadata> findByIsDeletedFalse();

    Page<FileMetadata> findByIsDeletedFalse(Pageable pageable);

}
