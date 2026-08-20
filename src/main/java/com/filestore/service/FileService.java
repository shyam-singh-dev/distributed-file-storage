package com.filestore.service;

import com.filestore.dto.FileMetadataDTO;
import com.filestore.dto.FileUploadResponse;
import com.filestore.dto.PageResponse;
import com.filestore.dto.ShareFileRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    FileUploadResponse uploadFile(
            MultipartFile file, String uploadedBy);

    InputStreamResource downloadFile(
            Long fileId, String requestedBy);

    PageResponse<FileMetadataDTO> getUserFiles(
            String userEmail, int page, int size);

    FileMetadataDTO getFileById(
            Long fileId, String userEmail);

    void deleteFile(Long fileId, String userEmail);

    // New methods:
    void shareFile(ShareFileRequest request, String ownerEmail);

    void unshareFile(Long fileId,
                     String sharedWithEmail,
                     String ownerEmail);

    List<FileMetadataDTO> getFilesSharedWithMe(String userEmail);

    InputStreamResource downloadSharedFile(
            Long fileId, String requestedBy);
}