package com.filestore.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuthResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserDTO user;

}
