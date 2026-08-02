package com.filestore.dto;

import lombok.*;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserDTO {

    private Long id;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;

}
