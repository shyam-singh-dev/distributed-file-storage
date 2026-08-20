package com.filestore.controller;

import com.filestore.dto.*;
import com.filestore.entity.FileMetadata;
import com.filestore.exception.ResourceNotFoundException;
import com.filestore.repository.FileMetadataRepository;
import com.filestore.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management",
        description = "APIs for file upload, download, management")
@SecurityRequirement(name = "Bearer Authentication")
public class FileController {

    private final FileService fileService;
    private final FileMetadataRepository fileMetadataRepository;

    // ─────────────────────────────────────
    // UPLOAD
    // ─────────────────────────────────────
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file",
            description = "Upload any file up to 100MB")
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String userEmail = authentication.getName();
        FileUploadResponse response =
                fileService.uploadFile(file, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "File uploaded successfully", response));
    }

    // ─────────────────────────────────────
    // DOWNLOAD (OWN FILE)
    // ─────────────────────────────────────
    @GetMapping("/download/{fileId}")
    @Operation(summary = "Download your own file")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable Long fileId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        FileMetadata metadata = fileMetadataRepository
                .findByIdAndUploadedByAndIsDeletedFalse(
                        fileId, userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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
    // DOWNLOAD SHARED FILE
    // ─────────────────────────────────────
    @GetMapping("/download/shared/{fileId}")
    @Operation(summary = "Download a file shared with you")
    public ResponseEntity<InputStreamResource> downloadShared(
            @PathVariable Long fileId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        FileMetadata metadata = fileMetadataRepository
                .findById(fileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "File not found with id: " + fileId));

        InputStreamResource resource =
                fileService.downloadSharedFile(
                        fileId, userEmail);

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
    // GET MY FILES (PAGINATED)
    // ─────────────────────────────────────
    @GetMapping("/my-files")
    @Operation(summary = "Get paginated list of my files")
    public ResponseEntity<ApiResponse<
            PageResponse<FileMetadataDTO>>> getMyFiles(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page")
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String userEmail = authentication.getName();

        PageResponse<FileMetadataDTO> files =
                fileService.getUserFiles(userEmail, page, size);

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
                ApiResponse.success("File details fetched", file));
    }

    // ─────────────────────────────────────
    // DELETE FILE
    // ─────────────────────────────────────
    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a file (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        fileService.deleteFile(fileId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.success("File deleted successfully"));
    }

    // ─────────────────────────────────────
    // SHARE FILE
    // ─────────────────────────────────────
    @PostMapping("/share")
    @Operation(summary = "Share a file with another user")
    public ResponseEntity<ApiResponse<Void>> shareFile(
            @Valid @RequestBody ShareFileRequest request,
            Authentication authentication) {

        String ownerEmail = authentication.getName();
        fileService.shareFile(request, ownerEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "File shared successfully with "
                                + request.getSharedWithEmail()));
    }

    // ─────────────────────────────────────
    // UNSHARE FILE
    // ─────────────────────────────────────
    @DeleteMapping("/{fileId}/share/{email}")
    @Operation(summary = "Remove file share from a user")
    public ResponseEntity<ApiResponse<Void>> unshareFile(
            @PathVariable Long fileId,
            @PathVariable String email,
            Authentication authentication) {

        String ownerEmail = authentication.getName();
        fileService.unshareFile(fileId, email, ownerEmail);

        return ResponseEntity.ok(
                ApiResponse.success("File unshared successfully"));
    }

    // ─────────────────────────────────────
    // GET FILES SHARED WITH ME
    // ─────────────────────────────────────
    @GetMapping("/shared-with-me")
    @Operation(summary = "Get all files shared with me")
    public ResponseEntity<ApiResponse<
            List<FileMetadataDTO>>> getSharedWithMe(
            Authentication authentication) {

        String userEmail = authentication.getName();
        List<FileMetadataDTO> files =
                fileService.getFilesSharedWithMe(userEmail);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Shared files fetched", files));
    }
}