package com.filestore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ShareFileRequest {

    @NotNull(message = "File ID is required")
    private Long fileId;

    @NotNull(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String sharedWithEmail;

}
