package com.filestore.service.impl;

import com.filestore.dto.FileMetadataDTO;
import com.filestore.dto.FileUploadResponse;
import com.filestore.entity.FileMetadata;
import com.filestore.exception.ResourceNotFoundException;
import com.filestore.repository.FileMetadataRepository;
import com.filestore.service.FileService;
import com.filestore.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final MinioService minioService;

    // ─────────────────────────────────────
    // UPLOAD FILE
    // ─────────────────────────────────────
    @Override
    @Transactional
    public FileUploadResponse uploadFile(
            MultipartFile file, String uploadedBy) {

        // Step 1: Validate file
        validateFile(file);

        log.info("Uploading file: {} by user: {}",
                file.getOriginalFilename(), uploadedBy);

        try {
            // Step 2: Upload to MinIO
            String objectName = minioService.uploadFile(file);

            // Step 3: Save metadata in MySQL
            FileMetadata metadata = FileMetadata.builder()
                    .fileName(objectName)
                    .originalName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .bucketName(minioService.getBucketName())
                    .objectName(objectName)
                    .uploadedBy(uploadedBy)
                    .isDeleted(false)
                    .build();

            FileMetadata saved =
                    fileMetadataRepository.save(metadata);

            log.info("File metadata saved with id: {}",
                    saved.getId());

            // Step 4: Return response
            return FileUploadResponse.builder()
                    .id(saved.getId())
                    .fileName(saved.getFileName())
                    .originalName(saved.getOriginalName())
                    .contentType(saved.getContentType())
                    .fileSize(saved.getFileSize())
                    .fileSizeReadable(
                            formatFileSize(saved.getFileSize()))
                    .uploadedBy(saved.getUploadedBy())
                    .uploadedAt(saved.getCreatedAt()
                            .format(DateTimeFormatter
                                    .ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();

        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage());
            throw new RuntimeException(
                    "File upload failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────
    // DOWNLOAD FILE
    // ─────────────────────────────────────
    @Override
    public InputStreamResource downloadFile(
            Long fileId, String requestedBy) {

        log.info("Download request for file id: {} by: {}",
                fileId, requestedBy);

        // Find metadata in MySQL
        FileMetadata metadata = fileMetadataRepository
                .findByIdAndUploadedByAndIsDeletedFalse(
                        fileId, requestedBy)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "File not found with id: " + fileId));

        try {
            // Get file stream from MinIO
            InputStream inputStream =
                    minioService.downloadFile(metadata.getObjectName());

            log.info("File download started: {}",
                    metadata.getOriginalName());

            return new InputStreamResource(inputStream);

        } catch (Exception e) {
            log.error("File download failed: {}", e.getMessage());
            throw new RuntimeException(
                    "File download failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────
    // GET USER FILES
    // ─────────────────────────────────────
    @Override
    public List<FileMetadataDTO> getUserFiles(String userEmail) {

        log.info("Fetching files for user: {}", userEmail);

        List<FileMetadata> files = fileMetadataRepository
                .findByUploadedByAndIsDeletedFalse(userEmail);

        return files.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────
    // GET FILE BY ID
    // ─────────────────────────────────────
    @Override
    public FileMetadataDTO getFileById(
            Long fileId, String userEmail) {

        FileMetadata metadata = fileMetadataRepository
                .findByIdAndUploadedByAndIsDeletedFalse(
                        fileId, userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "File not found with id: " + fileId));

        return convertToDTO(metadata);
    }

    // ─────────────────────────────────────
    // DELETE FILE (SOFT DELETE)
    // ─────────────────────────────────────
    @Override
    @Transactional
    public void deleteFile(Long fileId, String userEmail) {

        log.info("Delete request for file: {} by: {}",
                fileId, userEmail);

        FileMetadata metadata = fileMetadataRepository
                .findByIdAndUploadedByAndIsDeletedFalse(
                        fileId, userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "File not found with id: " + fileId));

        // Soft delete — mark as deleted, keep in DB
        metadata.setIsDeleted(true);
        fileMetadataRepository.save(metadata);

        log.info("File soft deleted: {}", fileId);

        // Also delete from MinIO
        try {
            minioService.deleteFile(metadata.getObjectName());
        } catch (Exception e) {
            log.error("MinIO delete failed: {}", e.getMessage());
            // Don't throw — DB is already updated
        }
    }

    // ─────────────────────────────────────
    // HELPER METHODS
    // ─────────────────────────────────────
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new RuntimeException(
                    "File size exceeds maximum limit of 100MB");
        }
    }

    private String formatFileSize(Long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.2f MB",
                    bytes / (1024.0 * 1024));
        return String.format("%.2f GB",
                bytes / (1024.0 * 1024 * 1024));
    }

    private FileMetadataDTO convertToDTO(FileMetadata metadata) {
        return FileMetadataDTO.builder()
                .id(metadata.getId())
                .fileName(metadata.getFileName())
                .originalName(metadata.getOriginalName())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .fileSizeReadable(
                        formatFileSize(metadata.getFileSize()))
                .uploadedBy(metadata.getUploadedBy())
                .createdAt(metadata.getCreatedAt())
                .isDeleted(metadata.getIsDeleted())
                .build();
    }
}