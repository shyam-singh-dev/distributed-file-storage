package com.filestore.service.impl;

import com.filestore.dto.PageResponse;
import com.filestore.dto.ShareFileRequest;
import com.filestore.entity.FileShare;
import com.filestore.repository.fileShareRepository;
import com.filestore.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private static final List<String> ALLOWED_CONTENT_TYPES =
            Arrays.asList(
                    "image/jpeg",
                    "image/png",
                    "image/gif",
                    "image/webp",
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument"
                            + ".wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument"
                            + ".spreadsheetml.sheet",
                    "text/plain",
                    "application/zip",
                    "video/mp4",
                    "audio/mpeg"
            );

    private final FileMetadataRepository fileMetadataRepository;
    private final MinioService minioService;

    private final fileShareRepository fileShareRepository;
    private final UserRepository userRepository;

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

    public PageResponse<FileMetadataDTO> getUserFiles(
            String userEmail, int page, int size) {

        log.info("Fetching files for: {} page: {}", userEmail, page);

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<FileMetadata> filePage = fileMetadataRepository
                .findByUploadedByAndIsDeletedFalse(
                        userEmail, pageable);

        List<FileMetadataDTO> content = filePage.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<FileMetadataDTO>builder()
                .content(content)
                .pageNumber(filePage.getNumber())
                .pageSize(filePage.getSize())
                .totalElements(filePage.getTotalElements())
                .totalPages(filePage.getTotalPages())
                .lastPage(filePage.isLast())
                .build();
    }


    // SHARE FILES

    @Override
    @Transactional
    public void shareFile(
            ShareFileRequest request, String ownerEmail) {

        log.info("Sharing file {} with {}",
                request.getFileId(), request.getSharedWithEmail());

        // Verify file belongs to owner
        FileMetadata file = fileMetadataRepository
                .findByIdAndUploadedByAndIsDeletedFalse(
                        request.getFileId(), ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "File not found with id: "
                                + request.getFileId()));

        // Cannot share with yourself
        if (ownerEmail.equals(request.getSharedWithEmail())) {
            throw new RuntimeException(
                    "You cannot share a file with yourself");
        }

        // Verify recipient exists
        if (!userRepository.existsByEmail(
                request.getSharedWithEmail())) {
            throw new ResourceNotFoundException(
                    "User not found: "
                            + request.getSharedWithEmail());
        }

        // Check already shared
        if (fileShareRepository.existsByFileIdAndSharedWithEmail(
                request.getFileId(),
                request.getSharedWithEmail())) {
            throw new RuntimeException(
                    "File already shared with this user");
        }

        // Create share record
        FileShare share = FileShare.builder()
                .file(file)
                .sharedByEmail(ownerEmail)
                .sharedWithEmail(request.getSharedWithEmail())
                .build();

        fileShareRepository.save(share);
        log.info("File {} shared successfully with {}",
                request.getFileId(), request.getSharedWithEmail());
    }

    // UNSHARE FILE

    @Override
    @Transactional
    public void unshareFile(
            Long fileId,
            String sharedWithEmail,
            String ownerEmail) {

        log.info("Unsharing file {} from {}",
                fileId, sharedWithEmail);

        // Verify file belongs to owner
        fileMetadataRepository
                .findByIdAndUploadedByAndIsDeletedFalse(
                        fileId, ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "File not found with id: " + fileId));

        fileShareRepository
                .deleteByFileIdAndSharedWithEmail(
                        fileId, sharedWithEmail);

        log.info("File {} unshared from {}",
                fileId, sharedWithEmail);
    }

    // Get shared files

    @Override
    public List<FileMetadataDTO> getFilesSharedWithMe(
            String userEmail) {

        log.info("Fetching files shared with: {}", userEmail);

        List<FileShare> shares = fileShareRepository
                .findBySharedWithEmail(userEmail);

        return shares.stream()
                .map(share -> convertToDTO(share.getFile()))
                .collect(Collectors.toList());
    }

    // Download shared files

    @Override
    public InputStreamResource downloadSharedFile(
            Long fileId, String requestedBy) {

        log.info("Shared file download request: {} by: {}",
                fileId, requestedBy);

        // Check if file is shared with this user
        FileShare share = fileShareRepository
                .findByFileIdAndSharedWithEmail(fileId, requestedBy)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shared file not found with id: " + fileId));

        FileMetadata metadata = share.getFile();

        if (metadata.getIsDeleted()) {
            throw new ResourceNotFoundException(
                    "File no longer available");
        }

        try {
            InputStream inputStream =
                    minioService.downloadFile(
                            metadata.getObjectName());
            return new InputStreamResource(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(
                    "File download failed: " + e.getMessage());
        }
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

        String contentType = file.getContentType();
        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException(
                    "File type not allowed: " + contentType
                            + ". Allowed: images, PDF, Office docs, "
                            + "text, zip, video, audio");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null
                || originalFilename.contains("..")) {
            throw new RuntimeException(
                    "Invalid file name");
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