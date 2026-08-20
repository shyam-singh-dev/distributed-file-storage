package com.filestore.controller;

import com.filestore.dto.ApiResponse;
import com.filestore.dto.FileMetadataDTO;
import com.filestore.dto.PageResponse;
import com.filestore.dto.UserDTO;
import com.filestore.entity.FileMetadata;
import com.filestore.entity.User;
import com.filestore.repository.FileMetadataRepository;
import com.filestore.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin",
        description = "Admin-only management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final FileMetadataRepository fileMetadataRepository;

    // ─────────────────────────────────────
    // GET ALL USERS
    // ─────────────────────────────────────
    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<ApiResponse<
            PageResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Admin: fetching all users");

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending());

        Page<User> userPage = userRepository.findAll(pageable);

        List<UserDTO> users = userPage.getContent()
                .stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());

        PageResponse<UserDTO> response =
                PageResponse.<UserDTO>builder()
                        .content(users)
                        .pageNumber(userPage.getNumber())
                        .pageSize(userPage.getSize())
                        .totalElements(userPage.getTotalElements())
                        .totalPages(userPage.getTotalPages())
                        .lastPage(userPage.isLast())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.success("Users fetched", response));
    }

    // ─────────────────────────────────────
    // GET ALL FILES
    // ─────────────────────────────────────
    @GetMapping("/files")
    @Operation(summary = "Get all files (Admin only)")
    public ResponseEntity<ApiResponse<
            PageResponse<FileMetadataDTO>>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Admin: fetching all files");

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending());

        Page<FileMetadata> filePage = fileMetadataRepository
                .findByIsDeletedFalse(pageable);

        List<FileMetadataDTO> files = filePage.getContent()
                .stream()
                .map(this::convertFileToDTO)
                .collect(Collectors.toList());

        PageResponse<FileMetadataDTO> response =
                PageResponse.<FileMetadataDTO>builder()
                        .content(files)
                        .pageNumber(filePage.getNumber())
                        .pageSize(filePage.getSize())
                        .totalElements(filePage.getTotalElements())
                        .totalPages(filePage.getTotalPages())
                        .lastPage(filePage.isLast())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.success("Files fetched", response));
    }

    // ─────────────────────────────────────
    // GET SYSTEM STATS
    // ─────────────────────────────────────
    @GetMapping("/stats")
    @Operation(summary = "Get system statistics (Admin only)")
    public ResponseEntity<ApiResponse<?>> getStats() {

        long totalUsers = userRepository.count();
        long totalFiles = fileMetadataRepository
                .findByIsDeletedFalse().size();

        var stats = new java.util.HashMap<String, Object>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalFiles", totalFiles);
        stats.put("generatedAt", LocalDateTime.now());

        return ResponseEntity.ok(
                ApiResponse.success("Stats fetched", stats));
    }

    // ─────────────────────────────────────
    // MAKE USER ADMIN
    // ─────────────────────────────────────
    @PutMapping("/users/{userId}/make-admin")
    @Operation(summary = "Promote user to ADMIN role")
    public ResponseEntity<ApiResponse<UserDTO>> makeAdmin(
            @PathVariable Long userId) {

        log.info("Admin: promoting user {} to ADMIN", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new com.filestore.exception
                                .ResourceNotFoundException(
                                "User not found: " + userId));

        user.setRole("ADMIN");
        User saved = userRepository.save(user);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User promoted to ADMIN",
                        convertUserToDTO(saved)));
    }

    // ─────────────────────────────────────
    // HELPER METHODS
    // ─────────────────────────────────────
    private UserDTO convertUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private FileMetadataDTO convertFileToDTO(
            FileMetadata metadata) {
        return FileMetadataDTO.builder()
                .id(metadata.getId())
                .fileName(metadata.getFileName())
                .originalName(metadata.getOriginalName())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .uploadedBy(metadata.getUploadedBy())
                .createdAt(metadata.getCreatedAt())
                .isDeleted(metadata.getIsDeleted())
                .build();
    }
}