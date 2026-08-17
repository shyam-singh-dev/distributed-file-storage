package com.filestore.controller;

import com.filestore.dto.ApiResponse;
import com.filestore.dto.FileMetadataDTO;
import com.filestore.dto.FileUploadResponse;
import com.filestore.entity.FileMetadata;
import com.filestore.repository.FileMetadataRepository;
import com.filestore.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management",
        description = "APIs for file upload, download and management")
@SecurityRequirement(name = "Bearer Authentication")
public class FileController {

    private final FileService fileService;
    private final FileMetadataRepository fileMetadataRepository;

    // ─────────────────────────────────────
    // UPLOAD FILE
    // ─────────────────────────────────────
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file",
            description = "Upload any file up to 100MB")
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("Upload request from: {}", userEmail);

        FileUploadResponse response =
                fileService.uploadFile(file, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "File uploaded successfully", response));
    }

    // ─────────────────────────────────────
    // DOWNLOAD FILE
    // ─────────────────────────────────────
    @GetMapping("/download/{fileId}")
    @Operation(summary = "Download a file by ID")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable Long fileId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("Download request for file: {} by: {}",
                fileId, userEmail);

        // Get file metadata for headers
        FileMetadata metadata = fileMetadataRepository
                .findByIdAndUploadedByAndIsDeletedFalse(
                        fileId, userEmail)
                .orElseThrow(() ->
                        new com.filestore.exception
                                .ResourceNotFoundException(
                                "File not found with id: " + fileId));

        InputStreamResource resource =
                fileService.downloadFile(fileId, userEmail);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + metadata.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(
                        metadata.getContentType()))
                .contentLength(metadata.getFileSize())
                .body(resource);
    }

    // ─────────────────────────────────────
    // GET MY FILES
    // ─────────────────────────────────────
    @GetMapping("/my-files")
    @Operation(summary = "Get all files uploaded by current user")
    public ResponseEntity<ApiResponse<List<FileMetadataDTO>>>
    getMyFiles(Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("Fetching files for: {}", userEmail);

        List<FileMetadataDTO> files =
                fileService.getUserFiles(userEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Files fetched successfully", files));
    }

    // ─────────────────────────────────────
    // GET FILE BY ID
    // ─────────────────────────────────────
    @GetMapping("/{fileId}")
    @Operation(summary = "Get file details by ID")
    public ResponseEntity<ApiResponse<FileMetadataDTO>> getFile(
            @PathVariable Long fileId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        FileMetadataDTO file =
                fileService.getFileById(fileId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "File details fetched", file));
    }

    // ─────────────────────────────────────
    // DELETE FILE
    // ─────────────────────────────────────
    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a file by ID")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("Delete request for file: {} by: {}",
                fileId, userEmail);

        fileService.deleteFile(fileId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.success("File deleted successfully"));
    }
}