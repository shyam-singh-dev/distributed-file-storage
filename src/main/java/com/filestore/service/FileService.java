package com.filestore.service;


import com.filestore.dto.FileMetadataDTO;
import com.filestore.dto.FileUploadResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    FileUploadResponse uploadFile(
            MultipartFile file, String uploadedBy
    );

    InputStreamResource downloadFile(Long fileId, String requestedBy);

    List<FileMetadataDTO> getUserFiles(String userEmail);

    FileMetadataDTO getFileById(Long fileId, String userEmail);

    void deleteFile(Long fileId,String userEmail);
}
