package com.filestore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class FileMetadataDTO {

    private Long id;
    private String fileName;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String fileSizeReadable;
    private String uploadedBy;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

}
