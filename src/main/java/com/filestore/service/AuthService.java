package com.filestore.service;


import com.filestore.dto.*;

public interface AuthService {
    UserDTO register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
    void logout(String token, String userEmail);
}
